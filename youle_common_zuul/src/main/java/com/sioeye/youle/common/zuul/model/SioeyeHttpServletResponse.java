package com.sioeye.youle.common.zuul.model;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class SioeyeHttpServletResponse extends HttpServletResponseWrapper {

    public SioeyeServletOutputStream sioeyeServletOutputStream;
    public PrintWriter teeWriter;
    public LogFilterCondition logFilterCondition;

    public SioeyeHttpServletResponse(HttpServletResponse httpServletResponse, LogFilterCondition logFilterCondition) {
        super(httpServletResponse);
        this.logFilterCondition = logFilterCondition;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (sioeyeServletOutputStream == null) {
            sioeyeServletOutputStream = new SioeyeServletOutputStream(this.getResponse(), logFilterCondition);
        }
        return sioeyeServletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (this.teeWriter == null) {
            this.teeWriter = new PrintWriter(
                    new OutputStreamWriter(getOutputStream(), this.getResponse().getCharacterEncoding()), true);
        }
        return this.teeWriter;
    }

    @Override
    public void flushBuffer() {
        if (this.teeWriter != null) {
            this.teeWriter.flush();
        }
    }

    public byte[] getOutputBuffer() {
        // teeServletOutputStream can be null if the getOutputStream method is
        // never
        // called.
        if (sioeyeServletOutputStream != null) {
            return sioeyeServletOutputStream.getOutputStreamAsByteArray();
        } else {
            return null;
        }
    }

    public void finish() throws IOException {
        if (this.teeWriter != null) {
            this.teeWriter.close();
        }
        if (this.sioeyeServletOutputStream != null) {
            this.sioeyeServletOutputStream.close();
        }
    }
}
