import React from 'react';

function ProductList({ products, onAddToCart, onViewDetail }) {
  const formatPrice = (price) => {
    if (!price) return '0';
    // BigDecimal이나 숫자를 안전하게 처리
    if (typeof price === 'object' && price !== null) {
      return Number(price).toLocaleString();
    }
    return Number(price).toLocaleString();
  };

  return (
    <div className="product-list">
      <h2>상품 목록</h2>
      <div className="product-grid">
        {products.map(product => (
          <div key={product.id} className="product-card">
            <h3
              onClick={() => onViewDetail(product.id)}
              style={{ cursor: 'pointer', color: '#4CAF50' }}
            >
              {product.name}
            </h3>
            <p className="description">{product.description}</p>
            <p className="price">₩{formatPrice(product.price)}</p>
            <p className="stock">재고: {product.stock}개</p>
            <p className="category">카테고리: {product.category}</p>
            <div className="product-actions">
              <button
                onClick={() => onViewDetail(product.id)}
                className="view-btn"
              >
                상세보기
              </button>
              <button
                onClick={() => onAddToCart(product.id)}
                disabled={product.stock === 0}
              >
                {product.stock > 0 ? '장바구니' : '품절'}
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

export default ProductList;