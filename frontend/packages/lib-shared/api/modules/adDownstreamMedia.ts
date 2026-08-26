import type { CordysAxios } from '@lib/shared/api/http/Axios';
import { AdDownstreamMediaUrl, AdDownstreamMediaPageUrl } from '@lib/shared/api/requrls/adDownstreamMedia';
import type { CommonList } from '@lib/shared/models/common';
import type { AdDownstreamMediaAccount } from '@lib/shared/models/advertising';

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

export interface AdDownstreamMediaDetail extends AdDownstreamMediaItem {
    accountList?: AdDownstreamMediaAccount[];
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
    accounts?: AdDownstreamMediaAccountSaveItem[];
}

export interface AdDownstreamMediaAccountSaveItem {
    id?: string;
    payeeName?: string;
    bankName?: string;
    bankAccount?: string;
    disabled?: number;
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
        return CDR.get<AdDownstreamMediaDetail>({ url: `${AdDownstreamMediaUrl}/${id}` });
    }
    function getAdDownstreamMediaAccounts(id: string) {
        return CDR.get<AdDownstreamMediaAccount[]>({ url: `${AdDownstreamMediaUrl}/${id}/accounts` });
    }
    function getAdDownstreamMediaPage(data: AdDownstreamMediaPageParams) {
        return CDR.post<AdDownstreamMediaPageResult>({ url: AdDownstreamMediaPageUrl, data }, { ignoreCancelToken: true });
    }
    function deleteAdDownstreamMedia(id: string) {
        return CDR.delete({ url: `${AdDownstreamMediaUrl}/${id}` });
    }
    return { createAdDownstreamMedia, updateAdDownstreamMedia, getAdDownstreamMediaDetail, getAdDownstreamMediaAccounts, getAdDownstreamMediaPage, deleteAdDownstreamMedia };
}
