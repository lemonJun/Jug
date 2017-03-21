package lemon.jug.web.servlet;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

import com.google.common.base.Preconditions;

public class XcafeServletOutputStream extends ServletOutputStream {

    private final OutputStream targetStream;

    private WriteListener writeListener;

    private volatile boolean closed = false;
    private volatile Boolean ready = Boolean.TRUE;

    /**
     * Create a DelegatingServletOutputStream for the given target stream.
     * @param targetStream the target stream (never {@code null})
     */
    public XcafeServletOutputStream(OutputStream targetStream) {
        Preconditions.checkNotNull(targetStream, "Target OutputStream must not be null");
        this.targetStream = targetStream;
    }

    /**
     * Return the underlying target stream (never {@code null}).
     */
    public final OutputStream getTargetStream() {
        return this.targetStream;
    }

    @Override
    public void write(int b) throws IOException {
        this.targetStream.write(b);
    }

    @Override
    public void flush() throws IOException {
        super.flush();
        this.targetStream.flush();
    }

    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
        this.targetStream.close();
    }

    @Override
    public boolean isReady() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        this.writeListener = writeListener;
    }

}