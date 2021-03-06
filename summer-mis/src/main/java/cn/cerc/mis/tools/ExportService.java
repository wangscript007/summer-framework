package cn.cerc.mis.tools;

import cn.cerc.core.DataSet;
import cn.cerc.core.IHandle;
import cn.cerc.core.Utils;
import cn.cerc.mis.client.IServiceProxy;
import cn.cerc.mis.client.ServiceFactory;
import cn.cerc.mis.core.AbstractForm;
import cn.cerc.mis.excel.output.AccreditException;
import cn.cerc.mis.excel.output.ExportExcel;
import cn.cerc.mis.other.BufferType;
import cn.cerc.mis.other.MemoryBuffer;
import jxl.write.WriteException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class ExportService extends ExportExcel {
    private String service;
    private String exportKey;
    private String corpNo;

    public String getCorpNo() {
        return corpNo;
    }

    public void setCorpNo(String corpNo) {
        this.corpNo = corpNo;
    }

    public ExportService(AbstractForm owner) {
        super(owner.getResponse());
        this.setHandle(owner);
        HttpServletRequest request = owner.getRequest();
        service = request.getParameter("service");
        exportKey = request.getParameter("exportKey");
    }

    @Override
    public void export() throws WriteException, IOException, AccreditException {
        if (service == null || "".equals(service)) {
            throw new RuntimeException("错误的调用：service is null");
        }
        if (exportKey == null || "".equals(exportKey)) {
            throw new RuntimeException("错误的调用：exportKey is null");
        }

        IHandle handle = (IHandle) this.getHandle();
        if (Utils.isEmpty(this.corpNo)) {
            this.corpNo = handle.getCorpNo();
        }
        IServiceProxy app = ServiceFactory.get(handle, this.corpNo);
        app.setService(service);
        try (MemoryBuffer buff = new MemoryBuffer(BufferType.getExportKey, handle.getUserCode(), exportKey)) {
            app.getDataIn().close();
            app.getDataIn().setJSON(buff.getString("data"));
        }
        if (!app.exec()) {
            this.export(app.getMessage());
            return;
        }

        DataSet dataOut = app.getDataOut();
        // 对分类进行处理
        dataOut.first();
        while (dataOut.fetch()) {
            if (dataOut.getBoolean("IsType_")) {
                dataOut.delete();
            }
        }
        this.getTemplate().setDataSet(dataOut);
        super.export();
    }
}
