import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdDownstreamMediaUrl, AdDownstreamMediaPageUrl } from '@lib/shared/api/requrls/adDownstreamMedia';
import type { CommonList } from '@lib/shared/models/common';

export interface AdDownstreamMediaItem {
    id: string;
    name?: string;
    mediaType?: string;
    mediaTypeLabel?: string;
    channel?: string;
    rateCard?: string;
    discountPolicy?: string;
    contactPerson?: string;
    contactPhone?: string;
    cooperationStatus?: number;
    status?: number;
    statusLabel?: string;
    businessEntityId?: string;
    businessEntityName?: string;
    createTime?: number;
}

export interface AdDownstreamMediaSaveParams {
    id?: string;
    name?: string;
    mediaType?: string;
    channel?: string;
    rateCard?: string;
    discountPolicy?: string;
    contactPerson?: string;
    contactPhone?: string;
    cooperationStatus?: number;
    status?: number;
    businessEntityId?: string;
}

type AdDownstreamMediaPageParams = Record<string, any>;
type AdDownstreamMediaPageResult = CommonList<AdDownstreamMediaItem>;

export default function useAdDownstreamMediaApi(CDR: CordysAxios) {
    function createAdDownstreamMedia(data: AdDownstreamMediaSaveParams) {
        return CDR.post<any>({ url: AdDownstreamMediaUrl, data });
    }
    function updateAdDownstreamMedia(data: AdDownstreamMediaSaveParams) {
        return CDR.put<any>({ url: AdDownstreamMediaUrl, data });
    }
    function getAdDownstreamMediaDetail(id: string) {
        return CDR.get<any>({ url: `${AdDownstreamMediaUrl}/${id}` });
    }
    function getAdDownstreamMediaPage(data: AdDownstreamMediaPageParams) {
        return CDR.post<AdDownstreamMediaPageResult>({ url: AdDownstreamMediaPageUrl, data }, { ignoreCancelToken: true });
    }
    function deleteAdDownstreamMedia(id: string) {
        return CDR.delete({ url: `${AdDownstreamMediaUrl}/${id}` });
    }
    return { createAdDownstreamMedia, updateAdDownstreamMedia, getAdDownstreamMediaDetail, getAdDownstreamMediaPage, deleteAdDownstreamMedia };
}
