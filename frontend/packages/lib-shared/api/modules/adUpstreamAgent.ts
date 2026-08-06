import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdUpstreamAgentUrl, AdUpstreamAgentPageUrl } from '@lib/shared/api/requrls/adUpstreamAgent';
import type { CommonList } from '@lib/shared/models/common';

export interface AdUpstreamAgentItem {
    id: string;
    name?: string;
    creditCode?: string;
    signingEntity?: string;
    contactPerson?: string;
    contactPhone?: string;
    cooperationStatus?: number;
    status?: number;
    statusLabel?: string;
    businessEntityId?: string;
    businessEntityName?: string;
    remark?: string;
    createTime?: number;
}

export interface AdUpstreamAgentSaveParams {
    id?: string;
    name?: string;
    creditCode?: string;
    signingEntity?: string;
    contactPerson?: string;
    contactPhone?: string;
    cooperationStatus?: number;
    status?: number;
    remark?: string;
    businessEntityId?: string;
}

type AdUpstreamAgentPageParams = Record<string, any>;
type AdUpstreamAgentPageResult = CommonList<AdUpstreamAgentItem>;

export default function useAdUpstreamAgentApi(CDR: CordysAxios) {
    function createAdUpstreamAgent(data: AdUpstreamAgentSaveParams) {
        return CDR.post<any>({ url: AdUpstreamAgentUrl, data });
    }
    function updateAdUpstreamAgent(data: AdUpstreamAgentSaveParams) {
        return CDR.put<any>({ url: AdUpstreamAgentUrl, data });
    }
    function getAdUpstreamAgentDetail(id: string) {
        return CDR.get<any>({ url: `${AdUpstreamAgentUrl}/${id}` });
    }
    function getAdUpstreamAgentPage(data: AdUpstreamAgentPageParams) {
        return CDR.post<AdUpstreamAgentPageResult>({ url: AdUpstreamAgentPageUrl, data }, { ignoreCancelToken: true });
    }
    function deleteAdUpstreamAgent(id: string) {
        return CDR.delete({ url: `${AdUpstreamAgentUrl}/${id}` });
    }
    return { createAdUpstreamAgent, updateAdUpstreamAgent, getAdUpstreamAgentDetail, getAdUpstreamAgentPage, deleteAdUpstreamAgent };
}
