#!/usr/bin/env python3
"""Validate and import the versioned catalog into Cloud Firestore."""

import argparse
import json
import math
import re
from pathlib import Path
from urllib.parse import urlsplit


CATALOG_PATH = (
    Path(__file__).resolve().parents[2]
    / "shared/src/commonMain/composeResources/files/catalog.json"
)


def fail(message: str) -> None:
    raise ValueError(message)


def slug(value: str) -> str:
    normalized = "".join(character.lower() if character.isalnum() else "-" for character in value)
    return re.sub(r"-+", "-", normalized).strip("-")


def web_url(value: object, field: str, record: str) -> str:
    if not isinstance(value, str) or not value.strip():
        fail(f"{field} inválida em {record}.")

    normalized = value.strip()
    if "://" not in normalized:
        normalized = f"https://{normalized}"

    try:
        parsed = urlsplit(normalized)
        if (
            parsed.scheme.lower() not in {"http", "https"}
            or not parsed.hostname
            or any(character.isspace() for character in normalized)
        ):
            fail(f"{field} inválida em {record}: {value!r}.")
        parsed.port
    except ValueError as error:
        fail(f"{field} inválida em {record}: {error}.")

    return normalized


def load_documents(source: Path) -> list[tuple[str, dict[str, object]]]:
    try:
        entries = json.loads(
            source.read_text(encoding="utf-8"),
            parse_constant=lambda value: fail(f"Constante JSON inválida: {value}."),
        )
    except OSError as error:
        fail(f"Não foi possível ler {source}: {error}.")
    except json.JSONDecodeError as error:
        fail(f"JSON inválido em {source}:{error.lineno}:{error.colno}: {error.msg}.")

    if not isinstance(entries, list):
        fail("O catálogo deve ser um array JSON.")

    stores: dict[str, dict[str, object]] = {}
    ids_by_name: dict[str, str] = {}
    seen_offers: dict[str, set[tuple[str, str, str, str]]] = {}
    allowed_fields = {"store", "cupon", "url", "discount", "type", "icon"}

    for index, entry in enumerate(entries, start=1):
        if not isinstance(entry, dict):
            fail(f"A oferta {index} não é um objeto JSON.")
        unknown_fields = set(entry) - allowed_fields
        if unknown_fields:
            fail(f"A oferta {index} contém campos desconhecidos: {sorted(unknown_fields)}.")

        name = entry.get("store")
        if not isinstance(name, str) or not name.strip():
            fail(f"A oferta {index} não possui nome de loja válido.")
        name = name.strip()
        store_id = slug(name)
        if not store_id:
            fail(f"O nome da loja da oferta {index} não gera um ID válido.")
        if store_id in ids_by_name and ids_by_name[store_id] != name:
            fail(f"As lojas {ids_by_name[store_id]!r} e {name!r} geram o mesmo ID '{store_id}'.")
        ids_by_name[store_id] = name

        discount = entry.get("discount")
        if isinstance(discount, bool) or not isinstance(discount, (str, int, float)):
            fail(f"O desconto da oferta {index} deve ser texto ou número.")
        if isinstance(discount, float) and not math.isfinite(discount):
            fail(f"O desconto da oferta {index} deve ser finito.")
        discount_value = str(discount).strip()
        if not discount_value:
            fail(f"O desconto da oferta {index} está vazio.")

        discount_type = entry.get("type")
        if not isinstance(discount_type, str) or not discount_type.strip():
            fail(f"A oferta {index} não possui tipo de desconto válido.")

        coupon = entry.get("cupon")
        if coupon is not None and not isinstance(coupon, str):
            fail(f"O cupom da oferta {index} deve ser texto ou nulo.")

        icon = entry.get("icon")
        if icon is not None and not isinstance(icon, str):
            fail(f"O ícone da oferta {index} deve ser texto ou nulo.")
        icon_url = web_url(icon, "Ícone", f"oferta {index}") if icon and icon.strip() else None

        affiliate_url = web_url(entry.get("url"), "Link", f"oferta {index}")
        coupon_code = coupon.strip() if coupon and coupon.strip() else ""
        offer_identity = (coupon_code, affiliate_url, discount_value, discount_type.strip())
        if offer_identity in seen_offers.setdefault(store_id, set()):
            fail(f"A oferta {index} está duplicada para a loja '{name}'.")
        seen_offers[store_id].add(offer_identity)

        if store_id not in stores:
            store: dict[str, object] = {
                "name": name,
                "offers": [],
            }
            if icon_url:
                store["iconUrl"] = icon_url
            stores[store_id] = store

        offers = stores[store_id]["offers"]
        assert isinstance(offers, list)
        offer_id = f"{store_id}-{len(offers)}"
        offer: dict[str, object] = {
            "id": offer_id,
            "affiliateUrl": affiliate_url,
            "discountValue": discount_value,
            "discountType": discount_type.strip(),
            "sortOrder": len(offers),
        }
        if coupon_code:
            offer["couponCode"] = coupon_code
        offers.append(offer)

        if "iconUrl" not in stores[store_id] and icon_url:
            stores[store_id]["iconUrl"] = icon_url

    return list(stores.items())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--project", help="Firebase project ID; defaults to the ADC project.")
    action = parser.add_mutually_exclusive_group()
    action.add_argument(
        "--apply",
        action="store_true",
        help="Write the validated documents. Without this flag, only report the planned import.",
    )
    action.add_argument(
        "--dry-run",
        action="store_true",
        help="Validate and report document IDs without writing (the default).",
    )
    args = parser.parse_args()

    documents = load_documents(CATALOG_PATH)
    offer_count = sum(len(document["offers"]) for _, document in documents)
    action_label = "Importando" if args.apply else "Simulação"
    print(f"{action_label}: {len(documents)} lojas e {offer_count} ofertas.")
    for store_id, document in documents:
        print(f"  stores/{store_id}: {len(document['offers'])} ofertas")

    if not args.apply:
        return

    try:
        from google.cloud import firestore
    except ImportError as error:
        raise SystemExit(
            "Dependência ausente. Instale com "
            "'python3 -m pip install -r tools/firestore/requirements.txt'."
        ) from error

    client = firestore.Client(project=args.project)
    batch = client.batch()
    pending = 0
    for store_id, document in documents:
        batch.set(client.collection("stores").document(store_id), document)
        pending += 1
        if pending == 450:
            batch.commit()
            batch = client.batch()
            pending = 0
    if pending:
        batch.commit()
    print("Importação concluída. Documentos ausentes da fonte não foram removidos.")


if __name__ == "__main__":
    try:
        main()
    except ValueError as error:
        raise SystemExit(str(error)) from error
