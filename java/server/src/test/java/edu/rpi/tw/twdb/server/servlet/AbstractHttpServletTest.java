package edu.rpi.tw.twdb.server.servlet;

import org.mockito.ArgumentCaptor;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

public abstract class AbstractHttpServletTest {
    protected final void setMockHttpServletRequestBody(final HttpServletRequest req, final String reqBody) throws IOException {
        final BufferedReader reqReader = mock(BufferedReader.class);
        when(req.getReader()).thenReturn(reqReader);
        when(reqReader.read(any(char[].class))).thenAnswer(invocation -> {
            final char[] dst = ((char[]) invocation.getArgument(0));
            System.arraycopy(reqBody.toCharArray(), 0, dst, 0, reqBody.length());
            return reqBody.length();
        }).thenReturn(-1);
    }

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

    protected final int getMockHttpServletErrorResponseCode(final HttpServletResponse resp) throws IOException {
        final ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(int.class);
        verify(resp).sendError(captor.capture());
        return captor.getValue();
    }

    protected final HttpServletResponse newMockHttpServletResponse() throws IOException {
        final HttpServletResponse resp = mock(HttpServletResponse.class);
        final ServletOutputStream respOutputStream = mock(ServletOutputStream.class);
        when(resp.getOutputStream()).thenReturn(respOutputStream);
        return resp;
    }
}
