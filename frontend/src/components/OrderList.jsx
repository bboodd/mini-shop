import React, { useState } from 'react';

function OrderList({ orders, onFetchOrders }) {
  const [email, setEmail] = useState('');

  const formatPrice = (price) => {
    if (!price) return 0;
    if (typeof price === 'object' && price !== null) {
      return Number(price);
    }
    return Number(price);
  };

  const handleFetch = () => {
    if (email) {
      onFetchOrders(email);
    }
  };

  return (
    <div className="orders">
      <h2>주문 내역</h2>

      <div className="order-search">
        <input
          type="email"
          placeholder="이메일로 주문 조회"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <button onClick={handleFetch}>조회</button>
      </div>

      <div className="order-list">
        {orders.map(order => (
          <div key={order.id} className="order-card">
            <div className="order-header">
              <h3>주문번호: {order.id}</h3>
              <span className={`status ${order.status.toLowerCase()}`}>
                {order.status}
              </span>
            </div>
            <p>주문자: {order.customerName}</p>
            <p>이메일: {order.customerEmail}</p>
            <p>총 금액: ₩{formatPrice(order.totalAmount).toLocaleString()}</p>
            <p>주문일: {new Date(order.createdAt).toLocaleString()}</p>

            <div className="order-items">
              <h4>주문 상품</h4>
              {order.orderItems.map(item => (
                <div key={item.id} className="order-item">
                  <span>{item.product.name}</span>
                  <span>{item.quantity}개</span>
                  <span>₩{formatPrice(item.price).toLocaleString()}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default OrderList;