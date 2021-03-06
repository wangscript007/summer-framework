package cn.cerc.mis.queue;

import cn.cerc.mis.rds.StubHandle;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AsyncServiceTest {

    @Test
    @Ignore
    public void test_send_get() {
        StubHandle handle = new StubHandle();
        AsyncService app = new AsyncService(handle);
        app.setService("TAppCreditLine.calCusCreditLimit");
        // app.setTimer(TDateTime.now().getTime());
        app.getDataIn().getHead().setField("UserCode_", handle.getUserCode());
        app.setSubject("回算信用额度");
        assertTrue("发送消息失败", app.exec());
    }
}
