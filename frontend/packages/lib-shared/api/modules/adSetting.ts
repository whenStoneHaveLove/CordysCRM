import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdSettingGetUrl, AdSettingSaveUrl } from '@lib/shared/api/requrls/adSetting';
import type { AdSettingInfo } from '@lib/shared/models/advertising';

export default function useAdSettingApi(CDR: CordysAxios) {
  // 获取系统配置（审批开关 / 账期规则，后端待补）
  function getAdSetting() {
    return CDR.get<AdSettingInfo>({ url: AdSettingGetUrl });
  }

  // 保存系统配置
  function saveAdSetting(data: AdSettingInfo) {
    return CDR.post<AdSettingInfo>({ url: AdSettingSaveUrl, data });
  }

  return { getAdSetting, saveAdSetting };
}
