package com.anjia.unidbgserver.unidbg;

import com.anjia.unidbgserver.utils.TempFileUtils;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.pointer.UnidbgPointer;
import com.github.unidbg.spi.SyscallHandler;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.github.unidbg.virtualmodule.android.JniGraphics;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LinkedMap;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

@Slf4j
public class IdleFQ extends AbstractJni implements IOResolver<AndroidFileIO> {

    // 资源路径常量
    private static final String BASE_PATH = "com/dragon/read/oversea/gp";
    private static final String APK_PATH = BASE_PATH + "/apk/番茄小说_6.8.1.32.apk";
    private static final String SO_METASEC_ML_PATH = BASE_PATH + "/lib/libmetasec_ml.so";
    private static final String SO_C_SHARE_PATH = BASE_PATH + "/lib/libc++_shared.so";
    private static final String ROOTFS_PATH = BASE_PATH + "/rootfs";
    private static final String MS_CERT_FILE_PATH = BASE_PATH + "/other/ms_16777218.bin";

    // 应用相关常量
    private static final String PACKAGE_NAME = "com.dragon.read.oversea.gp";
    private static final String APK_INSTALL_PATH = "/data/app/com.dragon.read.oversea.gp-q5NyjSN9BLSTVBJ54kg7YA==/base.apk";
    private static final int SDK_VERSION = 23;

    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private final Memory memory;
    private final DvmClass m;
    private final boolean loggable;

    // 临时文件缓存
    private File tempApkFile;
    private File tempSoMetasecMlFile;
    private File tempSoCShareFile;
    private File tempRootfsDir;
    private File tempMsCertFile;

    public IdleFQ(boolean loggable) {
        this.loggable = loggable;
        try {
            // 初始化临时文件
            initTempFiles();

            // 创建模拟器
            emulator = AndroidEmulatorBuilder
                .for64Bit()
                .setRootDir(tempRootfsDir)
                .setProcessName(PACKAGE_NAME)
                .addBackendFactory(new Unicorn2Factory(true))
                .build();

            // 设置inode和uid
            initEmulatorSettings();

            // 设置系统调用处理器
            SyscallHandler<AndroidFileIO> handler = emulator.getSyscallHandler();
            handler.setVerbose(false);
            handler.addIOResolver(this);

            // 初始化内存和VM
            memory = emulator.getMemory();
            memory.setLibraryResolver(new AndroidResolver(SDK_VERSION));

            vm = emulator.createDalvikVM();
            vm.setJni(this);
            vm.setVerbose(loggable);

            // 导入第三方虚拟模块
            new AndroidModule(emulator, vm).register(memory);
            new JniGraphics(emulator, vm).register(memory);

            // 载入依赖so库
            vm.loadLibrary(tempSoCShareFile, false);

            // 初始化JNI对应类
            m = vm.resolveClass("ms/bd/c/m");
            DvmClass a4a = vm.resolveClass("ms/bd/c/a4$a", m);
            DvmClass ms = vm.resolveClass("com/bytedance/mobsec/metasec/ml/MS", a4a);

            // 加载主要so库
            DalvikModule dm = vm.loadLibrary(tempSoMetasecMlFile, true);
            module = dm.getModule();
            dm.callJNI_OnLoad(emulator);

            log.info("IdleFQ初始化完成");
        } catch (Exception e) {
            log.error("IdleFQ初始化失败", e);
            throw new RuntimeException("IdleFQ初始化失败", e);
        }
    }

    /**
     * 初始化临时文件
     */
    private void initTempFiles() throws IOException {
        try {
            tempApkFile = TempFileUtils.getTempFile(APK_PATH);
            tempSoMetasecMlFile = TempFileUtils.getTempFile(SO_METASEC_ML_PATH);
            tempSoCShareFile = TempFileUtils.getTempFile(SO_C_SHARE_PATH);
            tempMsCertFile = TempFileUtils.getTempFile(MS_CERT_FILE_PATH);

            // 处理rootfs目录
            tempRootfsDir = createTempDir("fq_rootfs");

            if (loggable) {
                log.debug("临时APK文件: {}", tempApkFile.getAbsolutePath());
                log.debug("临时SO主文件: {}", tempSoMetasecMlFile.getAbsolutePath());
                log.debug("临时SO共享库文件: {}", tempSoCShareFile.getAbsolutePath());
                log.debug("临时证书文件: {}", tempMsCertFile.getAbsolutePath());
                log.debug("临时rootfs目录: {}", tempRootfsDir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("初始化临时文件失败", e);
            throw new IOException("初始化临时文件失败", e);
        }
    }

    /**
     * 创建临时目录
     */
    private File createTempDir(String prefix) throws IOException {
        File tempDir = File.createTempFile(prefix, "");
        tempDir.delete();
        if (!tempDir.mkdirs()) {
            throw new IOException("无法创建临时目录: " + tempDir);
        }
        tempDir.deleteOnExit();
        return tempDir;
    }

    /**
     * 初始化模拟器设置
     */
    private void initEmulatorSettings() {
        Map<String, Integer> iNode = new LinkedMap<>();
        iNode.put("/data/system", 671745);
        iNode.put("/data/app", 327681);
        iNode.put("/sdcard/android", 294915);
        iNode.put("/data/user/0/com.dragon.read.oversea.gp", 655781);
        iNode.put("/data/user/0/com.dragon.read.oversea.gp/files", 655864);
        emulator.set("inode", iNode);
        emulator.set("uid", 10074);

        // 可选的多线程支持配置
        // emulator.getBackend().registerEmuCountHook(100000);
        // emulator.getSyscallHandler().setVerbose(true);
        // emulator.getSyscallHandler().setEnableThreadDispatcher(true);
    }

    /**
     * 生成API请求签名
     *
     * @param url    API请求的URL
     * @param header HTTP请求头信息，格式为key\r\nvalue\r\n的字符串
     * @return 生成的签名字符串，失败时返回null
     */
    public String generateSignature(String url, String header) {
        try {
            if (loggable) {
                log.debug("准备生成签名 - URL: {}", url);
                log.debug("准备生成签名 - Header: {}", header);
            }

            // 调用native方法生成签名
            Number number = module.callFunction(emulator, 0x168c80, url, header);

            if (number == null) {
                log.error("调用native方法失败，返回结果为null");
                return null;
            }

            // 获取返回结果
            UnidbgPointer result = memory.pointer(number.longValue());
            if (result == null) {
                log.error("获取结果指针失败");
                return null;
            }

            String signature = result.getString(0);

            if (loggable) {
                log.debug("签名生成成功: {}", signature);
            }

            return signature;

        } catch (Exception e) {
            log.error("生成签名过程出错: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 重载方法：使用Map格式的header参数
     *
     * @param url       API请求的URL
     * @param headerMap HTTP请求头的Map，key为header名称，value为header值
     * @return 生成的签名字符串，失败时返回null
     */
    public String generateSignature(String url, Map<String, String> headerMap) {
        if (headerMap == null || headerMap.isEmpty()) {
            return generateSignature(url, "");
        }

        // 将Map转换为\r\n分隔的字符串格式
        StringBuilder headerBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            headerBuilder.append(entry.getKey()).append("\r\n")
                .append(entry.getValue()).append("\r\n");
        }

        // 移除最后的\r\n
        String header = headerBuilder.toString();
        if (header.endsWith("\r\n")) {
            header = header.substring(0, header.length() - 2);
        }

        return generateSignature(url, header);
    }

    /**
     * 简化的签名生成方法，只传入URL
     *
     * @param url API请求的URL
     * @return 生成的签名字符串，失败时返回null
     */
    public String generateSignature(String url) {
        return generateSignature(url, "");
    }

    // 环境补充相关方法
    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature) {
            case "com/bytedance/mobsec/metasec/ml/MS->b(IIJLjava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;": {
                int i = vaList.getIntArg(0);
                return handleMSMethod(vm, i);
            }
            case "java/lang/Thread->currentThread()Ljava/lang/Thread;":
                return vm.resolveClass("java/lang/Thread").newObject(Thread.currentThread());
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }

    /**
     * 处理MS方法调用
     */
    private DvmObject<?> handleMSMethod(BaseVM vm, int methodId) {
        switch (methodId) {
            case 65539:
                return new StringObject(vm, "/data/user/0/com/dragon/read/oversea/gp/files/.msdata");
            case 33554433:
            case 33554434:
                return DvmBoolean.valueOf(vm, true);
            case 16777232:
                return vm.resolveClass("java.lang.Integer").newObject(68132);
            case 16777233:
                return new StringObject(vm, "6.8.1.32");
            case 16777218: {
                // 返回证书文件的字节数组
                try {
                    if (tempMsCertFile != null && tempMsCertFile.exists()) {
                        byte[] fileData = Files.readAllBytes(tempMsCertFile.toPath());
                        if (loggable) {
                            log.debug("成功读取证书文件: {} bytes", fileData.length);
                        }
                        return new ByteArray(vm, fileData);
                    } else {
                        log.warn("证书文件不存在: {}", tempMsCertFile);
                        return null;
                    }
                } catch (IOException e) {
                    log.error("读取证书文件失败", e);
                    return null;
                }
            }
            case 268435470:
                // 返回当前时间戳
                return vm.resolveClass("java/lang/Long").newObject(System.currentTimeMillis());
            default:
                if (loggable) {
                    log.debug("未处理的MS方法ID: {}", methodId);
                }
                return null;
        }
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "java/lang/Thread->getStackTrace()[Ljava/lang/StackTraceElement;": {
                StackTraceElement[] elements = Thread.currentThread().getStackTrace();
                DvmObject[] objs = new DvmObject[elements.length];
                for (int i = 0; i < elements.length; i++) {
                    objs[i] = vm.resolveClass("java/lang/StackTraceElement").newObject(elements[i]);
                }
                return new ArrayObject(objs);
            }
            case "java/lang/StackTraceElement->getClassName()Ljava/lang/String;": {
                StackTraceElement element = (StackTraceElement) dvmObject.getValue();
                return new StringObject(vm, element.getClassName());
            }
            case "java/lang/StackTraceElement->getMethodName()Ljava/lang/String;": {
                StackTraceElement element = (StackTraceElement) dvmObject.getValue();
                return new StringObject(vm, element.getMethodName());
            }
            case "java/lang/Thread->getBytes(Ljava/lang/String;)[B": {
                String arg0 = (String) vaList.getObjectArg(0).getValue();
                if (loggable) {
                    log.debug("java/lang/Thread->getBytes arg0: {}", arg0);
                }
                return new ByteArray(vm, arg0.getBytes(StandardCharsets.UTF_8));
            }
            case "java/lang/Long->longValue()J": {
                Object value = dvmObject.getValue();
                if (value instanceof Long) {
                    return (DvmObject<Long>) value;
                }
            }
        }
        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public long callLongMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        if ("java/lang/Long->longValue()J".equals(signature)) {
            Object value = dvmObject.getValue();
            if (value instanceof Long) {
                return (Long) value;
            }
        }
        return super.callLongMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        if (loggable) {
            log.debug("getStaticIntField: {}", signature);
        }
        if ("com/bytedance/mobsec/metasec/ml/MS->a()V".equals(signature)) {
            return 0x40;
        }
        throw new UnsupportedOperationException(signature);
    }

    @Override
    public void callVoidMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if (loggable) {
            log.debug("callVoidMethod: {}", signature);
        }
        switch (signature) {
            case "com/bytedance/mobsec/metasec/ml/MS->a()V":
                if (loggable) {
                    log.debug("Patched: com/bytedance/mobsec/metasec/ml/MS->a()V");
                }
                return;
        }
        super.callVoidMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public int callIntMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        if ("java/lang/Integer->intValue()I".equals(signature)) {
            Object value = dvmObject.getValue();
            if (value instanceof Integer) {
                return (Integer) value;
            }
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
        }
        return super.callIntMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public boolean callBooleanMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        if ("java/lang/Boolean->booleanValue()Z".equals(signature)) {
            Object value = dvmObject.getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }
        return super.callBooleanMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        if (loggable) {
            log.debug("resolve ==> {}", pathname);
        }

        // 处理libmetasec_ml.so文件
        if (pathname.contains("libmetasec_ml.so")) {
            return FileResult.success(new SimpleFileIO(oflags, tempSoMetasecMlFile, pathname));
        }

        // 处理APK文件
        if (pathname.equals(APK_INSTALL_PATH)) {
            return FileResult.success(new SimpleFileIO(oflags, tempApkFile, pathname));
        }

        return null;
    }

    /**
     * 释放资源
     */
    public void destroy() {
        if (emulator != null) {
            try {
                emulator.close();
                log.info("IdleFQ资源已释放");
            } catch (Exception e) {
                log.error("关闭模拟器失败", e);
            }
        }
    }
}
