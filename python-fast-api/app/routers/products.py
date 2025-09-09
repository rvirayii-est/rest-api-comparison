from fastapi import APIRouter

router = APIRouter(prefix="/products", tags=["products"])

@router.get("")
async def list_products():
    return [
        {"id": "p1", "name": "Widget", "price": 4999},
        {"id": "p2", "name": "Gadget", "price": 7999},
    ]
