import { Link } from 'react-router-dom';
import { formatDateTime } from '../../../lib/formatters';
import { Cv } from '../cvApi';

type CvTableProps = {
  cvs: Cv[];
  renderActions?: (cv: Cv) => React.ReactNode;
};

export function CvTable({ cvs, renderActions }: CvTableProps) {
  if (cvs.length === 0) {
    return <p className="muted">No CVs found.</p>;
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Title</th>
            <th>Owner</th>
            <th>Summary</th>
            <th>Updated</th>
            {renderActions ? <th>Actions</th> : null}
          </tr>
        </thead>
        <tbody>
          {cvs.map((cv) => (
            <tr key={cv.id}>
              <td>
                <Link to={`/cvs/${cv.id}`}>{cv.title}</Link>
              </td>
              <td>{cv.ownerEmail}</td>
              <td>{cv.summary || 'No summary'}</td>
              <td>{formatDateTime(cv.updatedAt)}</td>
              {renderActions ? <td>{renderActions(cv)}</td> : null}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
