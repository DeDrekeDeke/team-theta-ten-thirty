import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../components/Button';
import { ErrorMessage } from '../../components/ErrorMessage';
import { LoadingState } from '../../components/LoadingState';
import { PageHeader } from '../../components/PageHeader';
import { CvTable } from './components/CvTable';
import { Cv, listArchivedCvs, softDeleteCv, unarchiveCv } from './cvApi';

export function CvArchivedPage() {
  const [cvs, setCvs] = useState<Cv[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    loadArchivedCvs();
  }, []);

  async function loadArchivedCvs() {
    setLoading(true);
    setError('');
    try {
      setCvs(await listArchivedCvs());
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not load archived CVs');
    } finally {
      setLoading(false);
    }
  }

  async function handleRestore(cv: Cv) {
    setError('');
    setNotice('');
    try {
      await unarchiveCv(cv.id);
      setCvs((currentCvs) => currentCvs.filter((item) => item.id !== cv.id));
      setNotice(`${cv.title} was restored.`);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not restore CV');
    }
  }

  async function handleSoftDelete(cv: Cv) {
    setError('');
    setNotice('');
    try {
      await softDeleteCv(cv.id);
      setCvs((currentCvs) => currentCvs.filter((item) => item.id !== cv.id));
      setNotice(`${cv.title} was removed.`);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not remove CV');
    }
  }

  return (
    <section className="page-section">
      <PageHeader
        title="Archived CVs"
        description="CVs hidden from the active list. Restore them when they should become active again."
        actions={<Link className="button secondary" to="/">Active CVs</Link>}
      />

      {notice ? <p className="notice-message">{notice}</p> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {loading ? (
        <LoadingState />
      ) : (
        <CvTable
          cvs={cvs}
          renderActions={(cv) => (
            <div className="row-actions">
              <Button type="button" onClick={() => handleRestore(cv)}>
                Restore
              </Button>
              <Button type="button" variant="secondary" onClick={() => handleSoftDelete(cv)}>
                Remove
              </Button>
            </div>
          )}
        />
      )}
    </section>
  );
}
