import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../components/Button';
import { ErrorMessage } from '../../components/ErrorMessage';
import { PageHeader } from '../../components/PageHeader';
import { getCurrentUser, logout } from '../auth/authStore';
import { deleteOwnAccount } from './accountApi';

export function AccountSettingsPage() {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState('');

  async function handleDeleteAccount() {
    const confirmed = window.confirm('Delete your account? Your CVs will be hidden too.');
    if (!confirmed) {
      return;
    }

    let deleted = false;
    setError('');
    setDeleting(true);

    try {
      await deleteOwnAccount();
      deleted = true;
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not delete account');
    } finally {
      setDeleting(false);
    }

    if (deleted) {
      logout('Your account was deleted.');
      navigate('/login', { replace: true });
    }
  }

  return (
    <section className="page-section narrow">
      <PageHeader title="Account" description="Manage your sign-in account." />

      {error ? <ErrorMessage message={error} /> : null}

      <div className="panel account-summary">
        <div>
          <span className="muted">Email</span>
          <strong>{user?.email}</strong>
        </div>
        <div>
          <span className="muted">Role</span>
          <strong>{user?.role === 'ADMIN' ? 'Admin' : 'User'}</strong>
        </div>
      </div>

      <div className="panel danger-zone">
        <div>
          <h3>Delete account</h3>
          <p className="muted">Your account and CVs will stop appearing in the app.</p>
        </div>
        <Button type="button" variant="danger" disabled={deleting} onClick={handleDeleteAccount}>
          {deleting ? 'Deleting...' : 'Delete account'}
        </Button>
      </div>
    </section>
  );
}
