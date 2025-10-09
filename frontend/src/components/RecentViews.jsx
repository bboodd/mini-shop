import React from 'react';

function RecentViews({ recentViews, onAddToCart, onViewDetail, onRemove, onClearAll }) {
  const formatPrice = (price) => {
    if (!price) return '0';
    if (typeof price === 'object' && price !== null) {
      return Number(price).toLocaleString();
    }
    return Number(price).toLocaleString();
  };

  return (
    <div className="recent-views">
      <div className="recent-header">
        <h2>최근 본 상품 ({recentViews.length}개)</h2>
        {recentViews.length > 0 && (
          <button onClick={onClearAll} className="clear-btn">
            전체 삭제
          </button>
        )}
      </div>

      {recentViews.length === 0 ? (
        <p className="empty-message">최근 본 상품이 없습니다.</p>
      ) : (
        <div className="product-grid">
          {recentViews.map(product => (
            <div key={product.id} className="product-card recent-item">
              <button
                className="remove-recent"
                onClick={() => onRemove(product.id)}
                title="삭제"
              >
                ✕
              </button>
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
                  다시보기
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
      )}
    </div>
  );
}

export default RecentViews;