import React, { useState } from 'react';

function Cart({ cart, onUpdateQuantity, onRemove, onCheckout }) {
  const [customerName, setCustomerName] = useState('');
  const [customerEmail, setCustomerEmail] = useState('');

  const formatPrice = (price) => {
    if (!price) return 0;
    if (typeof price === 'object' && price !== null) {
      return Number(price);
    }
    return Number(price);
  };

  const totalAmount = cart.reduce((sum, item) =>
    sum + (formatPrice(item.price) * item.quantity), 0
  );

  const handleCheckout = () => {
    if (!customerName || !customerEmail) {
      alert('이름과 이메일을 입력해주세요.');
      return;
    }
    onCheckout(customerName, customerEmail);
  };

  return (
    <div className="cart">
      <h2>장바구니</h2>

      {cart.length === 0 ? (
        <p>장바구니가 비어있습니다.</p>
      ) : (
        <>
          <div className="cart-items">
            {cart.map(item => (
              <div key={item.productId} className="cart-item">
                <div className="item-info">
                  <h3>{item.productName}</h3>
                  <p>₩{formatPrice(item.price).toLocaleString()}</p>
                </div>
                <div className="item-controls">
                  <input
                    type="number"
                    min="1"
                    value={item.quantity}
                    onChange={(e) => onUpdateQuantity(item.productId, parseInt(e.target.value))}
                  />
                  <button onClick={() => onRemove(item.productId)}>삭제</button>
                </div>
                <div className="item-total">
                  ₩{(formatPrice(item.price) * item.quantity).toLocaleString()}
                </div>
              </div>
            ))}
          </div>

          <div className="cart-summary">
            <h3>총 금액: ₩{totalAmount.toLocaleString()}</h3>
          </div>

          <div className="checkout-form">
            <h3>주문자 정보</h3>
            <input
              type="text"
              placeholder="이름"
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
            />
            <input
              type="email"
              placeholder="이메일"
              value={customerEmail}
              onChange={(e) => setCustomerEmail(e.target.value)}
            />
            <button onClick={handleCheckout} className="checkout-btn">
              주문하기
            </button>
          </div>
        </>
      )}
    </div>
  );
}

export default Cart;