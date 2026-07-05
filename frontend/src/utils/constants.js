export const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

export const REFRESH_PATH = '/auth/refresh';

export const PAGE_SIZE = 20;

export const DOCUMENT_TYPES = {
  LUAT: 'Luật',
  NGHI_DINH: 'Nghị định',
  THONG_TU: 'Thông tư',
  QUYET_DINH: 'Quyết định',
  QCVN: 'Quy chuẩn Việt Nam',
  TCVN: 'Tiêu chuẩn Việt Nam',
  CONG_VAN: 'Công văn',
  KHAC: 'Khác',
};

export const DOCUMENT_STATUS = {
  CON_HIEU_LUC: 'Còn hiệu lực',
  HET_HIEU_LUC: 'Hết hiệu lực',
  CHUA_CO_HIEU_LUC: 'Chưa có hiệu lực',
  HET_HIEU_LUC_MOT_PHAN: 'Hết hiệu lực một phần',
};

export const RELATION_TYPES = {
  GUIDED_BY: 'Được hướng dẫn bởi',
  GUIDES: 'Hướng dẫn cho',
  AMENDED_BY: 'Sửa đổi bởi',
  AMENDS: 'Sửa đổi',
  REPLACES: 'Thay thế',
  REPLACED_BY: 'Bị thay thế',
  RELATED: 'Liên quan',
};

export const SKIP_RESTORE_KEY = 'cll_skip_restore';

export const FILTER_CACHE_TTL = 5 * 60 * 1000;
