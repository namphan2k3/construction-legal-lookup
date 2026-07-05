import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { Input } from '@/components/common/Input/Input';
import { Button } from '@/components/common/Button/Button';
import { register as registerUser } from '@/services/auth.service';
import { getErrorMessage } from '@/services/api';
import toast from 'react-hot-toast';
import styles from './AuthPage.module.css';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const { register, handleSubmit, formState: { errors } } = useForm({
    defaultValues: { email: '', password: '', fullName: '' },
  });

  const onSubmit = async (data) => {
    setLoading(true);
    setError('');
    try {
      await registerUser(data);
      toast.success('Đăng ký thành công');
      navigate('/');
    } catch (err) {
      const msg = getErrorMessage(err, 'Đăng ký thất bại');
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
          <h1 className={styles.auth__title}>Đăng ký</h1>
          <p className={styles.auth__subtitle}>
            Tạo tài khoản để lưu văn bản và sử dụng AI
          </p>
        </div>

        {error ? <p className={styles.auth__error}>{error}</p> : null}

        <form className={styles.auth__form} onSubmit={handleSubmit(onSubmit)} noValidate>
          <Input
            label="Họ và tên"
            type="text"
            autoComplete="name"
            error={errors.fullName?.message}
            {...register('fullName')}
          />
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
          <Input
            label="Mật khẩu"
            type="password"
            autoComplete="new-password"
            error={errors.password?.message}
            {...register('password', {
              required: 'Vui lòng nhập mật khẩu',
              minLength: { value: 8, message: 'Mật khẩu tối thiểu 8 ký tự' },
            })}
          />
          <Button type="submit" variant="primary" fullWidth loading={loading} className={styles.auth__submit}>
            Đăng ký
          </Button>
        </form>

        <p className={styles.auth__footer}>
          Đã có tài khoản? <Link to="/login">Đăng nhập</Link>
        </p>
      </div>
    </div>
  );
}
