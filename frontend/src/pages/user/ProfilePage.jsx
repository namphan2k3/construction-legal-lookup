import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Input } from '@/components/common/Input/Input';
import { Button } from '@/components/common/Button/Button';
import { useAuthStore } from '@/stores/authStore';
import { updateProfile, changePassword } from '@/services/auth.service';
import { getErrorMessage } from '@/services/api';
import toast from 'react-hot-toast';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const user = useAuthStore((s) => s.user);
  const [profileLoading, setProfileLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [showPasswordForm, setShowPasswordForm] = useState(false);

  const profileForm = useForm({
    defaultValues: { fullName: user?.fullName || '' },
  });

  const passwordForm = useForm({
    defaultValues: { currentPassword: '', newPassword: '', confirmPassword: '' },
  });

  const onProfileSubmit = async (data) => {
    setProfileLoading(true);
    try {
      await updateProfile({ fullName: data.fullName });
      toast.success('Cập nhật hồ sơ thành công');
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setProfileLoading(false);
    }
  };

  const onPasswordSubmit = async (data) => {
    if (data.newPassword !== data.confirmPassword) {
      passwordForm.setError('confirmPassword', { message: 'Mật khẩu xác nhận không khớp' });
      return;
    }
    setPasswordLoading(true);
    try {
      await changePassword({
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      });
      toast.success('Đổi mật khẩu thành công. Vui lòng đăng nhập lại.');
      passwordForm.reset();
      setShowPasswordForm(false);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setPasswordLoading(false);
    }
  };

  return (
    <div className="page">
      <div className="container">
        <header className="page__header">
          <h1 className="page__title">Hồ sơ cá nhân</h1>
          <p className="page__subtitle">Quản lý thông tin tài khoản và mật khẩu</p>
        </header>

        <div className={styles.profile}>
          <section className={styles.profile__section}>
            <h2 className={styles.profile__sectionTitle}>Thông tin cá nhân</h2>
            <div className={styles.profile__emailLabel}>Email</div>
            <div className={styles.profile__email}>{user?.email}</div>
            <form
              className={styles.profile__form}
              onSubmit={profileForm.handleSubmit(onProfileSubmit)}
              style={{ marginTop: '1.5rem' }}
            >
              <Input
                label="Họ và tên"
                error={profileForm.formState.errors.fullName?.message}
                {...profileForm.register('fullName')}
              />
              <Button type="submit" variant="primary" loading={profileLoading}>
                Lưu thay đổi
              </Button>
            </form>
          </section>

          <section className={styles.profile__section}>
            <h2 className={styles.profile__sectionTitle}>Bảo mật</h2>
            {!showPasswordForm ? (
              <Button variant="secondary" onClick={() => setShowPasswordForm(true)}>
                Đổi mật khẩu
              </Button>
            ) : (
              <>
                <form className={styles.profile__form} onSubmit={passwordForm.handleSubmit(onPasswordSubmit)}>
                  <Input
                    label="Mật khẩu hiện tại"
                    type="password"
                    autoComplete="current-password"
                    error={passwordForm.formState.errors.currentPassword?.message}
                    {...passwordForm.register('currentPassword', { required: 'Vui lòng nhập mật khẩu hiện tại' })}
                  />
                  <Input
                    label="Mật khẩu mới"
                    type="password"
                    autoComplete="new-password"
                    error={passwordForm.formState.errors.newPassword?.message}
                    {...passwordForm.register('newPassword', {
                      required: 'Vui lòng nhập mật khẩu mới',
                      minLength: { value: 8, message: 'Tối thiểu 8 ký tự' },
                    })}
                  />
                  <Input
                    label="Xác nhận mật khẩu mới"
                    type="password"
                    autoComplete="new-password"
                    error={passwordForm.formState.errors.confirmPassword?.message}
                    {...passwordForm.register('confirmPassword', { required: 'Vui lòng xác nhận mật khẩu' })}
                  />
                  <div className={styles.formActions}>
                    <Button type="submit" variant="primary" loading={passwordLoading}>
                      Đổi mật khẩu
                    </Button>
                    <Button
                      type="button"
                      variant="secondary"
                      onClick={() => {
                        setShowPasswordForm(false);
                        passwordForm.reset();
                      }}
                    >
                      Hủy
                    </Button>
                  </div>
                </form>
              </>
            )}
          </section>
        </div>
      </div>
    </div>
  );
}
