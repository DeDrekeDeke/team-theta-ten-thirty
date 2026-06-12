import { FormEvent, useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button } from '../../components/Button';
import { ErrorMessage } from '../../components/ErrorMessage';
import { LoadingState } from '../../components/LoadingState';
import { PageHeader } from '../../components/PageHeader';
import { CvTable } from './components/CvTable';
import { CvListItem, archiveCv, Cv, listCvs, searchCvs, softDeleteCv } from './cvApi';

type LocationState = {
  notice?: string;
};

export function CvListPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const [cvs, setCvs] = useState<CvListItem[]>([]);
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    const state = location.state as LocationState | null;
    if (state?.notice) {
      setNotice(state.notice);
      navigate(location.pathname, { replace: true, state: null });
    }
    loadCvs({ clearNotice: !state?.notice });
  }, []);

  async function loadCvs(options: { clearNotice?: boolean } = {}) {
    setLoading(true);
    setError('');
    if (options.clearNotice ?? true) {
      setNotice('');
    }
    try {
      setCvs(await listCvs());
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not load CVs');
    } finally {
      setLoading(false);
    }
  }

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError('');
    setNotice('');
    try {
      setCvs(query.trim() ? await searchCvs(query) : await listCvs());
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Search failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleArchive(cv: Cv) {
    setError('');
    setNotice('');
    try {
      await archiveCv(cv.id);
      setCvs((currentCvs) => currentCvs.filter((item) => item.id !== cv.id));
      setNotice(`${cv.title} was archived.`);
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : 'Could not archive CV');
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
        title="CVs"
        description="Structured CVs available to the current user."
        actions={<Link className="button primary" to="/create">Create CV</Link>}
      />

      <form className="toolbar" onSubmit={handleSearch}>
        <input
          className="text-input"
          placeholder="Search title, owner, or summary"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
        <Button type="submit" variant="secondary">
          Search
        </Button>
      </form>

      {notice ? <p className="notice-message">{notice}</p> : null}
      {error ? <ErrorMessage message={error} /> : null}
      {loading ? (
        <LoadingState />
      ) : (
        <CvTable
          cvs={cvs}
          renderActions={(cv) => (
            <div className="row-actions">
              <Button type="button" variant="secondary" onClick={() => handleArchive(cv)}>
                Archive
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
