import { Outlet } from 'react-router-dom';
import { Header } from '@/components/layout/Header/Header';
import { Footer } from '@/components/layout/Footer/Footer';
import FloatingChatbot from '@/components/FloatingChatbot';
import styles from './MainLayout.module.css';

export function MainLayout() {
  return (
    <div className={styles.layout}>
      <Header />
      <main className={styles.layout__main}>
        <Outlet />
      </main>
      <Footer />
      <FloatingChatbot />
    </div>
  );
}
