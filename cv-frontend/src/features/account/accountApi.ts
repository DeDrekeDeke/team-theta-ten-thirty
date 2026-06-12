import { apiRequest } from '../../app/apiClient';

export function deleteOwnAccount() {
  return apiRequest<void>('/api/account', {
    method: 'DELETE'
  });
}
