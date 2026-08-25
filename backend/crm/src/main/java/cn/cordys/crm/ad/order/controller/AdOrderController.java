package cn.cordys.crm.ad.order.controller;

import cn.cordys.common.constants.PermissionConstants;
import cn.cordys.common.dto.ExportHeadDTO;
import cn.cordys.common.pager.PagerWithOption;
import cn.cordys.common.permission.CsPermission;
import cn.cordys.common.exception.GenericException;
import cn.cordys.context.OrganizationContext;
import cn.cordys.crm.ad.common.constants.OrderStatus;
import cn.cordys.crm.ad.common.constants.OrderType;
import cn.cordys.crm.ad.common.constants.PaymentMethod;
import cn.cordys.crm.ad.common.constants.ReceiptMethod;
import cn.cordys.crm.ad.order.dto.request.AdOrderApproveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderForceArchiveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderPageRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderSaveRequest;
import cn.cordys.crm.ad.order.dto.request.AdOrderVoidRequest;
import cn.cordys.crm.ad.order.dto.response.AdOrderDetailResponse;
import cn.cordys.crm.ad.order.dto.response.AdOrderListResponse;
import cn.cordys.crm.ad.order.domain.AdOrder;
import cn.cordys.crm.ad.order.service.AdOrderService;
import cn.cordys.security.SessionUtils;
import cn.idev.excel.EasyExcel;
import cn.idev.excel.write.style.HorizontalCellStyleStrategy;
import cn.idev.excel.write.metadata.style.WriteCellStyle;
import cn.idev.excel.write.metadata.style.WriteFont;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 广告订单控制器（M2 订单核心闭环，V3.1 §13.2）。
 *
 * <p>所有写操作均在 {@link AdOrderService} 中标注 {@code @OperationLog}（写入 ad_operation_log）。
 * 响应由框架 {@code ResultResponseBodyAdvice} 统一包裹（包路径 cn.cordys）。
 * 接口级权限由 {@link CsPermission} 依据 {@link PermissionConstants} 的 AD_ORDER_* 码校验（B-2）。</p>
 */
@Tag(name = "广告订单")
@RestController
@RequestMapping("/api/ad/order")
public class AdOrderController {

    @Resource
    private AdOrderService adOrderService;

    private String userId() {
        return SessionUtils.getUserId();
    }

    private String orgId() {
        return OrganizationContext.getOrganizationId();
    }

    @PostMapping
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "新建订单（草稿）")
    public AdOrder create(@RequestBody AdOrderSaveRequest request) {
        return adOrderService.create(request, userId(), orgId());
    }

    @PutMapping
    @CsPermission(PermissionConstants.AD_ORDER_CREATE)
    @Operation(summary = "编辑草稿订单")
    public AdOrder update(@RequestBody AdOrderSaveRequest request) {
        return adOrderService.update(request, userId(), orgId());
    }

    @GetMapping("/{id}")
    @CsPermission(PermissionConstants.AD_ORDER_READ)
    @Operation(summary = "订单详情（订单+附件+改单历史+操作记录+可见动作）")
    public AdOrderDetailResponse detail(@PathVariable("id") String id) {
        return adOrderService.detail(id, userId(), orgId());
    }

    @PostMapping("/page")
    @CsPermission(PermissionConstants.AD_ORDER_READ)
    @Operation(summary = "订单分页（多筛选+关键字+主体隔离+缺合同标记）")
    public PagerWithOption<List<AdOrderListResponse>> page(@RequestBody AdOrderPageRequest request) {
        return adOrderService.page(request, userId(), orgId());
    }

    @PostMapping("/export")
    @CsPermission(PermissionConstants.AD_ORDER_EXPORT)
    @Operation(summary = "导出订单（按筛选条件，同步流式返回 Excel 文件）")
    public void export(@RequestBody AdOrderPageRequest request, HttpServletResponse response) {
        List<AdOrderListResponse> data = adOrderService.exportList(request, userId(), orgId());
        List<List<String>> head = buildExportHead(request);
        List<List<Object>> rows = buildExportRows(head, data);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            String fileName = "广告订单_" + System.currentTimeMillis() + ".xlsx";
            response.setHeader("Content-disposition",
                    "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
            EasyExcel.write(response.getOutputStream())
                    .head(head)
                    .registerWriteHandler(buildColumnWidthStrategy())
                    .registerWriteHandler(buildHorizontalStyleStrategy())
                    .sheet("广告订单")
                    .doWrite(rows);
        } catch (Exception e) {
            throw new GenericException("导出失败：" + e.getMessage());
        }
    }

    /**
     * 列宽策略：按表头计算（中英字符自适应），给数据较长的字段适当放宽。
     * 覆盖项目内置 CustomHeadColWidthStyleStrategy 的偏窄算法。
     */
    private cn.idev.excel.write.style.column.AbstractHeadColumnWidthStyleStrategy buildColumnWidthStrategy() {
        return new cn.idev.excel.write.style.column.AbstractHeadColumnWidthStyleStrategy() {
            @Override
            protected Integer columnWidth(cn.idev.excel.metadata.Head head, Integer columnIndex) {
                String headName = head.getHeadNameList().getFirst();
                int width = 0;
                for (char c : headName.toCharArray()) {
                    width += (c >= 0x4e00 && c <= 0x9fa5) ? 3 : 2;
                }
                // 标题宽度 + 一定富余；最小不低于 12，最大不超过 60
                int result = width + 6;
                return Math.min(Math.max(result, 12), 60);
            }
        };
    }

    /**
     * 表头居中加粗深灰底白字 + 数据行细边框 + 垂直居中。
     * 项目内置 HorizontalCellStyleStrategy 根据行号自动切换 head/content 样式。
     */
    private HorizontalCellStyleStrategy buildHorizontalStyleStrategy() {
        // 表头样式：深灰底白字、加粗、11号、居中
        WriteCellStyle headStyle = new WriteCellStyle();
        headStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        headStyle.setFillPatternType(FillPatternType.SOLID_FOREGROUND);
        WriteFont headFont = new WriteFont();
        headFont.setFontName("微软雅黑");
        headFont.setFontHeightInPoints((short) 11);
        headFont.setBold(true);
        headFont.setColor(IndexedColors.WHITE.getIndex());
        headStyle.setWriteFont(headFont);
        headStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 数据样式：细边框、10号、垂直居中、左对齐
        WriteCellStyle contentStyle = new WriteCellStyle();
        WriteFont contentFont = new WriteFont();
        contentFont.setFontName("微软雅黑");
        contentFont.setFontHeightInPoints((short) 10);
        contentStyle.setWriteFont(contentFont);
        contentStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        contentStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);

        return new HorizontalCellStyleStrategy(headStyle, contentStyle);
    }

    /**
     * 导出表头：前端有传则按前端 key/title；否则使用与列表页一致的默认列。
     */
    private List<List<String>> buildExportHead(AdOrderPageRequest request) {
        List<List<String>> head = new ArrayList<>();
        if (request.getHeadList() != null && !request.getHeadList().isEmpty()) {
            for (ExportHeadDTO h : request.getHeadList()) {
                head.add(Arrays.asList(h.getTitle()));
            }
        } else {
            for (String title : DEFAULT_HEAD_TITLES) {
                head.add(Arrays.asList(title));
            }
        }
        return head;
    }

    /**
     * 每行数据按 headList 的 key 顺序，调用对应取值器；没有的列跳过。
     */
    private List<List<Object>> buildExportRows(List<List<String>> head, List<AdOrderListResponse> data) {
        List<List<Object>> rows = new ArrayList<>();
        for (AdOrderListResponse r : data) {
            List<Object> row = new ArrayList<>();
            for (List<String> colHead : head) {
                String title = colHead.get(0);
                Function<AdOrderListResponse, Object> mapper = HEAD_VALUE_MAPPER.get(title);
                Object value = mapper == null ? null : mapper.apply(r);
                row.add(value == null ? "" : value);
            }
            rows.add(row);
        }
        return rows;
    }

    /**
     * 默认导出列（与前端列表页保持一致，排除操作列）。
     */
    private static final List<String> DEFAULT_HEAD_TITLES = Arrays.asList(
            "订单编号", "订单名称", "业务主体", "客户", "订单类型", "状态",
            "订单总金额", "应收金额", "媒体应付总额", "返点金额",
            "收款方式", "付款方式", "投放起始日", "投放结束日", "创建时间",
            "收款状态", "付款状态", "合同状态"
    );

    /**
     * 标题 → 取值函数映射。可读值转换（金额去科学计数法、日期格式化、状态中文等）。
     */
    private static final Map<String, Function<AdOrderListResponse, Object>> HEAD_VALUE_MAPPER =
            new HashMap<>();

    static {
        HEAD_VALUE_MAPPER.put("订单编号", AdOrderListResponse::getOrderNo);
        HEAD_VALUE_MAPPER.put("订单名称", AdOrderListResponse::getOrderName);
        HEAD_VALUE_MAPPER.put("业务主体", AdOrderListResponse::getBusinessEntityName);
        HEAD_VALUE_MAPPER.put("客户", AdOrderListResponse::getCustomerName);
        HEAD_VALUE_MAPPER.put("订单类型", r -> {
            if (r.getOrderType() == null) return "";
            return OrderType.labelOf(r.getOrderType());
        });
        HEAD_VALUE_MAPPER.put("状态", r -> {
            if (r.getStatus() == null) return "";
            return OrderStatus.labelOf(r.getStatus());
        });
        HEAD_VALUE_MAPPER.put("订单总金额", r -> {
            if (r.getTotalAmount() == null) return "";
            return r.getTotalAmount().toPlainString();
        });
        HEAD_VALUE_MAPPER.put("应收金额", r -> {
            if (r.getReceivableAmount() == null) return "";
            return r.getReceivableAmount().toPlainString();
        });
        HEAD_VALUE_MAPPER.put("媒体应付", r -> {
            if (r.getMediaPayableAmount() == null) return "";
            return r.getMediaPayableAmount().toPlainString();
        });
        HEAD_VALUE_MAPPER.put("返点金额", r -> {
            if (r.getRebateAmount() == null) return "";
            return r.getRebateAmount().toPlainString();
        });
        HEAD_VALUE_MAPPER.put("收款方式", r -> {
            if (r.getReceiptMethod() == null) return "";
            return ReceiptMethod.labelOf(r.getReceiptMethod());
        });
        HEAD_VALUE_MAPPER.put("付款方式", r -> {
            if (r.getPaymentMethod() == null) return "";
            return PaymentMethod.labelOf(r.getPaymentMethod());
        });
        HEAD_VALUE_MAPPER.put("投放起始", r -> {
            if (r.getDeliveryStartDate() == null) return "";
            return new SimpleDateFormat("yyyy-MM-dd").format(r.getDeliveryStartDate());
        });
        HEAD_VALUE_MAPPER.put("投放结束", r -> {
            if (r.getDeliveryEndDate() == null) return "";
            return new SimpleDateFormat("yyyy-MM-dd").format(r.getDeliveryEndDate());
        });
        HEAD_VALUE_MAPPER.put("创建时间", r -> {
            if (r.getCreateTime() == null) return "";
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(r.getCreateTime()));
        });
        HEAD_VALUE_MAPPER.put("收款状态", r -> {
            if (r.getReceiptDone() == null) return "";
            return r.getReceiptDone() == 1 ? "已收款" : "未收款";
        });
        HEAD_VALUE_MAPPER.put("付款状态", r -> {
            if (r.getPaymentDone() == null) return "";
            return r.getPaymentDone() == 1 ? "已支付" : "未支付";
        });
        HEAD_VALUE_MAPPER.put("合同状态", r -> {
            if (r.getMissingContract() == null) return "";
            return r.getMissingContract() == 1 ? "未提交" : "已提交";
        });
    }

    @PostMapping("/{id}/submit")
    @CsPermission(PermissionConstants.AD_ORDER_SUBMIT)
    @Operation(summary = "提交（0→10；L-14 关闭时 0→20）")
    public AdOrder submit(@PathVariable("id") String id) {
        return adOrderService.submit(id, userId(), orgId());
    }

    @PostMapping("/{id}/approve")
    @CsPermission(PermissionConstants.AD_ORDER_APPROVE)
    @Operation(summary = "管理组审核（通过 10→20 / 驳回 10→0）")
    public AdOrder approve(@PathVariable("id") String id, @RequestBody AdOrderApproveRequest request) {
        return adOrderService.approve(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/confirm-execute")
    @CsPermission(PermissionConstants.AD_ORDER_CONFIRM_EXECUTE)
    @Operation(summary = "确认执行（45→50）")
    public AdOrder confirmExecute(@PathVariable("id") String id) {
        return adOrderService.confirmExecute(id, userId(), orgId());
    }

    @PostMapping("/{id}/void")
    @CsPermission(PermissionConstants.AD_ORDER_VOID)
    @Operation(summary = "作废（→100；保留附件 L-21，红冲标记 L-27）")
    public AdOrder voidOrder(@PathVariable("id") String id, @RequestBody AdOrderVoidRequest request) {
        return adOrderService.voidOrder(id, request, userId(), orgId());
    }

    @PostMapping("/{id}/force-archive")
    @CsPermission(PermissionConstants.AD_ORDER_FORCE_ARCHIVE)
    @Operation(summary = "强制归档（80→90，管理组，带坏账金额 L-13）")
    public AdOrder forceArchive(@PathVariable("id") String id, @RequestBody AdOrderForceArchiveRequest request) {
        return adOrderService.forceArchive(id, request, userId(), orgId());
    }
}
