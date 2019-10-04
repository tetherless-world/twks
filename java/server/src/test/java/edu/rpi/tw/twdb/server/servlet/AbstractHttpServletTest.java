package edu.rpi.tw.twdb.server.servlet;

import org.mockito.ArgumentCaptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.mockito.Mockito.*;

public abstract class AbstractHttpServletTest {
    protected final String getMockHttpServletResponseBody(final HttpServletResponse resp) throws IOException {
        final ArgumentCaptor<byte[]> respBytesCaptor = ArgumentCaptor.forClass(byte[].class);
        final ArgumentCaptor<Integer> respOffsetCaptor = ArgumentCaptor.forClass(int.class);
        final ArgumentCaptor<Integer> respLenCaptor = ArgumentCaptor.forClass(int.class);
        verify(resp.getOutputStream()).write(respBytesCaptor.capture(), respOffsetCaptor.capture(), respLenCaptor.capture());
        final byte[] respBytes = respBytesCaptor.getValue();
        final Integer respOffset = respOffsetCaptor.getValue();
        final Integer respLen = respLenCaptor.getValue();
        return new String(respBytes, respOffset, respLen);
    }

    protected final HttpServletResponse newMockHttpServletResponse() throws IOException {
        final HttpServletResponse resp = mock(HttpServletResponse.class);
        final ServletOutputStream respOutputStream = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(respOutputStream);
        return resp;
    }
}
