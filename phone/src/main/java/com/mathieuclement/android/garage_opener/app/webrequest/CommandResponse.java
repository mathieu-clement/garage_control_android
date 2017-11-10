package com.mathieuclement.android.garage_opener.app.webrequest;

import com.google.gson.annotations.SerializedName;

/**
 * @author Mathieu Clément
 * @since 05.08.2013
 */
class CommandResponse {
    private boolean success;

    @SerializedName("phone_number_ok")
    private boolean phoneNumberOk;

    @SerializedName("account_ok")
    private boolean accountOk;

    @SerializedName("error_msg")
    private String errorMessage;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isPhoneNumberOk() {
        return phoneNumberOk;
    }

    public void setPhoneNumberOk(boolean phoneNumberOk) {
        this.phoneNumberOk = phoneNumberOk;
    }

    public boolean isAccountOk() {
        return accountOk;
    }

    public void setAccountOk(boolean accountOk) {
        this.accountOk = accountOk;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
