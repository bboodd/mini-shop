import React, { useState, useEffect } from 'react';
import ProductList from './components/ProductList';
import Cart from './components/Cart';
import OrderList from './components/OrderList';
import SearchBar from './components/SearchBar';
import RecentViews from './components/RecentViews';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api';

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [orders, setOrders] = useState([]);
  const [recentViews, setRecentViews] = useState([]);
  const [currentView, setCurrentView] = useState('products');
  const [userId] = useState('user123'); // 간단하게 고정 userId 사용

  useEffect(() => {
    fetchProducts();
    fetchCart();
    fetchRecentViews();
  }, []);

  const fetchProducts = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/products`);
      const data = await response.json();
      setProducts(data);
    } catch (error) {
      console.error('Error fetching products:', error);
    }
  };

  const fetchCart = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/cart/${userId}`);
      const data = await response.json();
      setCart(data);
    } catch (error) {
      console.error('Error fetching cart:', error);
    }
  };

  const fetchRecentViews = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/recent-views/${userId}`);
      const data = await response.json();
      setRecentViews(data);
    } catch (error) {
      console.error('Error fetching recent views:', error);
    }
  };

  const searchProducts = async (keyword) => {
    try {
      const response = await fetch(`${API_BASE_URL}/products/search?keyword=${keyword}`);
      const data = await response.json();
      setProducts(data);
    } catch (error) {
      console.error('Error searching products:', error);
    }
  };

  const viewProductDetail = async (productId) => {
    try {
      // 상품 조회 시 최근 본 상품에 자동 추가
      const response = await fetch(`${API_BASE_URL}/products/${productId}?userId=${userId}`);
      const product = await response.json();

      // 최근 본 상품 목록 갱신
      fetchRecentViews();

      const formatPrice = (price) => {
        if (!price) return '0';
        if (typeof price === 'object' && price !== null) {
          return Number(price).toLocaleString();
        }
        return Number(price).toLocaleString();
      };

      alert(`${product.name}\n\n${product.description}\n\n가격: ₩${formatPrice(product.price)}\n재고: ${product.stock}개`);
    } catch (error) {
      console.error('Error viewing product:', error);
    }
  };

  const addToCart = async (productId, quantity = 1) => {
    try {
      await fetch(`${API_BASE_URL}/cart`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, productId, quantity })
      });
      fetchCart();
      alert('장바구니에 추가되었습니다!');
    } catch (error) {
      console.error('Error adding to cart:', error);
      alert('장바구니 추가 실패');
    }
  };

  const updateCartItem = async (productId, quantity) => {
    try {
      await fetch(`${API_BASE_URL}/cart`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, productId, quantity })
      });
      fetchCart();
    } catch (error) {
      console.error('Error updating cart:', error);
    }
  };

  const removeFromCart = async (productId) => {
    try {
      await fetch(`${API_BASE_URL}/cart/${userId}/${productId}`, {
        method: 'DELETE'
      });
      fetchCart();
    } catch (error) {
      console.error('Error removing from cart:', error);
    }
  };

  const createOrder = async (customerName, customerEmail) => {
    try {
      const response = await fetch(`${API_BASE_URL}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, customerName, customerEmail })
      });
      const order = await response.json();
      alert('주문이 완료되었습니다!');
      fetchCart();
      setCurrentView('orders');
      fetchOrders(customerEmail);
    } catch (error) {
      console.error('Error creating order:', error);
      alert('주문 실패: ' + error.message);
    }
  };

  const fetchOrders = async (email) => {
    try {
      const response = await fetch(`${API_BASE_URL}/orders/customer/${email}`);
      const data = await response.json();
      setOrders(data);
    } catch (error) {
      console.error('Error fetching orders:', error);
    }
  };

  const removeRecentView = async (productId) => {
    try {
      await fetch(`${API_BASE_URL}/recent-views/${userId}/${productId}`, {
        method: 'DELETE'
      });
      fetchRecentViews();
    } catch (error) {
      console.error('Error removing recent view:', error);
    }
  };

  const clearRecentViews = async () => {
    try {
      await fetch(`${API_BASE_URL}/recent-views/${userId}`, {
        method: 'DELETE'
      });
      fetchRecentViews();
    } catch (error) {
      console.error('Error clearing recent views:', error);
    }
  };

  return (
    <div className="App">
      <header className="header">
        <h1>🛒 미니 쇼핑몰</h1>
        <nav>
          <button onClick={() => setCurrentView('products')}>상품목록</button>
          <button onClick={() => setCurrentView('recent')}>
            최근 본 상품 ({recentViews.length})
          </button>
          <button onClick={() => setCurrentView('cart')}>
            장바구니 ({cart.length})
          </button>
          <button onClick={() => setCurrentView('orders')}>주문내역</button>
        </nav>
      </header>

      <main className="main-content">
        {currentView === 'products' && (
          <>
            <SearchBar onSearch={searchProducts} onReset={fetchProducts} />
            <ProductList
              products={products}
              onAddToCart={addToCart}
              onViewDetail={viewProductDetail}
            />
          </>
        )}

        {currentView === 'recent' && (
          <RecentViews
            recentViews={recentViews}
            onAddToCart={addToCart}
            onViewDetail={viewProductDetail}
            onRemove={removeRecentView}
            onClearAll={clearRecentViews}
          />
        )}

        {currentView === 'cart' && (
          <Cart
            cart={cart}
            onUpdateQuantity={updateCartItem}
            onRemove={removeFromCart}
            onCheckout={createOrder}
          />
        )}

        {currentView === 'orders' && (
          <OrderList orders={orders} onFetchOrders={fetchOrders} />
        )}
      </main>
    </div>
  );
}

export default App;