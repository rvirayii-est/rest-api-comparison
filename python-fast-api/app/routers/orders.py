from fastapi import APIRouter, Depends
from ..deps import get_current_user, require_roles

router = APIRouter(prefix="/orders", tags=["orders"])

@router.get("")
async def all_orders(user=Depends(require_roles("ADMIN"))):
    return [{"id": "o1", "userId": "u1", "productId": "p1", "status": "PAID"}]

@router.get("/me")
async def my_orders(user=Depends(get_current_user)):
    return [{"id": "oX", "userId": user["sub"], "productId": "p2", "status": "PAID"}]
