package org.apache.dubbo.admin.model.dto;

import org.apache.dubbo.admin.common.util.Constants;

import java.util.Objects;

import static org.apache.dubbo.admin.common.util.Constants.COLON;

/**
 * 服务状态DTO
 *
 * @author xiansuo.dai by 2023/3/15 10:19
 */
public class ServerStatusDTO {

    /**
     * 应用名称
     */
    private String application;


    /**
     * IP地址
     */
    private String ip;


    /**
     * 端口
     */
    private Integer port;

    /**
     * Tag
     */
    private String tag;

    /**
     * 暴露服务数量
     */
    private Integer exportServices;

    /**
     * 状态异常的 服务数量
     */
    private Integer exceptionServices;


    /**
     * 状态
     */
    private Boolean status;


    /**
     * 不使用hash值，是因为不可读。
     *
     * @return String
     */
    public String uniqueKey() {
        return getApplication() + COLON + getIp() + COLON + getPort() + COLON + getTag();
    }


    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getExportServices() {
        return exportServices;
    }

    public void setExportServices(Integer exportServices) {
        this.exportServices = exportServices;
    }

    public Integer getExceptionServices() {
        return exceptionServices;
    }

    public void setExceptionServices(Integer exceptionServices) {
        this.exceptionServices = exceptionServices;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServerStatusDTO)) return false;
        ServerStatusDTO dto = (ServerStatusDTO) o;
        return getApplication().equals(dto.getApplication()) && getIp().equals(dto.getIp()) && getPort().equals(dto.getPort()) && Objects.equals(getTag(), dto.getTag());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApplication(), getIp(), getPort(), getTag());
    }
}
