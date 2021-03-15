package com.scb.rider.tax.constants;

import com.scb.rider.tax.exception.RecipientListEmptyException;

public final class ErrorConstants {

    private ErrorConstants() {
    }

    public static final String TYPE_MISMATCH_EX_MSG = "api.tax.typeMisMatch.msg";
    public static final String MISSING_PART_EX_MSG = "api.tax.missingPart.msg";
    public static final String MISSING_PARAM_EX_MSG = "api.tax.missingRequestParameter.msg";
    public static final String ARGUMENT_MISMATCH_EX_MSG = "api.tax.argumentMismatch.msg";
    public static final String NO_HANDLER_EX_MSG = "api.tax.noHandler.msg";
    public static final String NO_HTTP_METHOD_EX_MSG = "api.tax.noHttpMethod.msg";
    public static final String MEDIA_NOT_SUPPORT_EX_MSG = "api.tax.mediaNotSupport.msg";
    public static final String SERVER_ERROR_EX_MSG = "api.tax.serverError.msg";
    public static final String DATA_NOT_FOUND_EX_MSG = "api.tax.dataNotFound.msg";
    public static final String AWS_S3_EX_MSG = "api.tax.s3.ex.msg";
    public static final String EXTERNAL_SERVICE_INVOCATION_FAILED_EX_MSG = "api.tax.external.service.invocation.failed.ex.msg";
    public static final String SFTP_CONNECTION_ERROR_MSG = "api.tax.sftp.connection.error.msg";
    public static final String OPS_SERVICE_ERROR_MSG = "api.tax.ops.service.error.msg";
    public static final String RIDER_SERVICE_ERROR_MSG = "api.tax.rider.service.error.msg";
    public static final String RECONCILIATION_SERVICE_ERROR_MSG = "api.tax.reconciliation.service.error.msg";
    public static final String NO_MATCHED_DATA_ERROR_MSG = "api.tax.no.matched.data.error.msg";
    public static final String SETTLEMENT_SERVICE_ERROR_MSG = "api.tax.settlement.service.error.msg";
    public static final String MESSAGING_ERROR_MSG = "api.tax.messaging.service.error.msg";
    public static final String RECIPIENT_LIST_EMPTY_ERROR_MSG = "api.tax.recipient.empty.error.msg";
    public static final String EXCEL_GENERATION_ERROR_MSG = "api.tax.excel.generation.error.msg";

}
