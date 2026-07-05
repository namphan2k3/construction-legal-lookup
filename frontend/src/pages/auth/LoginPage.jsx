import { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Input } from '@/components/common/Input/Input';
import { Button } from '@/components/common/Button/Button';
import { login } from '@/services/auth.service';
import { getErrorMessage } from '@/services/api';
import toast from 'react-hot-toast';
import styles from './AuthPage.module.css';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const from = location.state?.from || '/';

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: { email: '', password: '' },
  });

  const onSubmit = async (data) => {
    setLoading(true);
    setError('');
    try {
      await login(data);
      toast.success('Đăng nhập thành công');
      navigate(from, { replace: true });
    } catch (err) {
      const msg = getErrorMessage(err, 'Đăng nhập thất bại');
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={`page ${styles.auth}`}>
      <div className={styles.auth__card}>
        <div className={styles.auth__header}>
          <h1 className={styles.auth__title}>Đăng nhập</h1>
          <p className={styles.auth__subtitle}>
            Truy cập bookmark, lịch sử và hỗ trợ AI
          </p>
        </div>

        {error ? <p className={styles.auth__error}>{error}</p> : null}

        <form className={styles.auth__form} onSubmit={handleSubmit(onSubmit)} noValidate>
          <Input
            label="Email"
            type="email"
            autoComplete="email"
            error={errors.email?.message}
            {...register('email', {
              required: 'Vui lòng nhập email',
              pattern: { value: /^\S+@\S+\.\S+$/, message: 'Email không hợp lệ' },
            })}
          />
          <div style={{ position: 'relative' }}>
            <Input
              label="Mật khẩu"
              type={showPassword ? 'text' : 'password'}
              autoComplete="current-password"
              error={errors.password?.message}
              {...register('password', { required: 'Vui lòng nhập mật khẩu' })}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              style={{
                position: 'absolute',
                right: '12px',
                top: '38px',
                background: 'none',
                border: 'none',
                cursor: 'pointer',
                color: '#666',
                fontSize: '14px',
              }}
            >
              {showPassword ? 'Ẩn' : 'Hiện'}
            </button>
          </div>
          <Button type="submit" variant="primary" fullWidth loading={loading} className={styles.auth__submit}>
            Đăng nhập
          </Button>
        </form>

        <p className={styles.auth__footer}>
          Chưa có tài khoản? <Link to="/register">Đăng ký ngay</Link>
        </p>
      </div>
    </div>
  );
}
