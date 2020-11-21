package com.sioeye.youle.common.zuul.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;

public class SioeyeServletOutputStream extends ServletOutputStream {

    public ServletOutputStream underlyingStream;
    public ByteArrayOutputStream baosCopy;
    private LogFilterCondition logFilterCondition;
    private Long sioeyeZuulLogMaxSize;

    public SioeyeServletOutputStream(ServletResponse httpServletResponse, LogFilterCondition logFilterCondition)
            throws IOException {
        // System.out.println("TeeServletOutputStream.constructor() called");
        this.underlyingStream = httpServletResponse.getOutputStream();
        baosCopy = new ByteArrayOutputStream();
        this.logFilterCondition = logFilterCondition;
        this.sioeyeZuulLogMaxSize = logFilterCondition.getSioeyeZuulLogMaxSize();
    }

    public byte[] getOutputStreamAsByteArray() {
        return baosCopy.toByteArray();
    }

    @Override
    public void write(int val) throws IOException {
        if (underlyingStream != null) {
            underlyingStream.write(val);
            // 根据enable,length判断是否输出返回值到logback-access
            if (!logFilterCondition.getSioeyeZuulLogEnable() || baosCopy != null) {
                baosCopy = null;
            } else {
                if (baosCopy != null) {
                    baosCopy.write(val);
                }
            }
        }
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        if (underlyingStream == null) {
            return;
        }
        // System.out.println("WRITE TeeServletOutputStream.write(byte[])
        // called");
        write(byteArray, 0, byteArray.length);
    }

    @Override
    public void write(byte byteArray[], int offset, int length) throws IOException {
        if (underlyingStream == null) {
            return;
        }
        // System.out.println("WRITE TeeServletOutputStream.write(byte[], int,
        // int)
        // called");
        // System.out.println(new String(byteArray, offset, length));
        underlyingStream.write(byteArray, offset, length);
        // 根据enable,length判断是否输出返回值到logback-access
        if (!logFilterCondition.getSioeyeZuulLogEnable()) {
            baosCopy = null;
        } else {
            if (length > sioeyeZuulLogMaxSize || logFilterCondition.getSioeyeZuulLogMaxSize() < 0) {
                logFilterCondition.setSioeyeZuulLogMaxSize(0);
                return;
            } else {
                baosCopy.write(byteArray, offset, length);
                logFilterCondition.setSioeyeZuulLogMaxSize(logFilterCondition.getSioeyeZuulLogMaxSize() - length);
            }
        }
    }

    @Override
    public void close() throws IOException {
        // System.out.println("CLOSE TeeServletOutputStream.close() called");

        // If the servlet accessing the stream is using a writer instead of
        // an OutputStream, it will probably call os.close() before calling
        // writer.close. Thus, the underlying output stream will be called
        // before the data sent to the writer could be flushed.
    }

    @Override
    public void flush() throws IOException {
        if (underlyingStream == null) {
            return;
        }
        // System.out.println("FLUSH TeeServletOutputStream.flush() called");
        underlyingStream.flush();
        if (baosCopy == null) {
            return;
        }
        baosCopy.flush();
    }

    @Override
    public boolean isReady() {
        throw new RuntimeException("Not yet implemented");
    }

    @Override
    public void setWriteListener(WriteListener listener) {
        throw new RuntimeException("Not yet implemented");
    }

}
