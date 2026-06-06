package hr.algebra.iis.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * =============================================
 * KORAK 4: gRPC servis za DHMZ temperaturu
 * =============================================
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.63.0)",
    comments = "Source: weather.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class WeatherServiceGrpc {

  private WeatherServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "hr.algebra.iis.grpc.WeatherService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest,
      hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTemperature",
      requestType = hr.algebra.iis.grpc.CityRequest.class,
      responseType = hr.algebra.iis.grpc.TemperatureResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest,
      hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureMethod() {
    io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest, hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureMethod;
    if ((getGetTemperatureMethod = WeatherServiceGrpc.getGetTemperatureMethod) == null) {
      synchronized (WeatherServiceGrpc.class) {
        if ((getGetTemperatureMethod = WeatherServiceGrpc.getGetTemperatureMethod) == null) {
          WeatherServiceGrpc.getGetTemperatureMethod = getGetTemperatureMethod =
              io.grpc.MethodDescriptor.<hr.algebra.iis.grpc.CityRequest, hr.algebra.iis.grpc.TemperatureResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTemperature"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hr.algebra.iis.grpc.CityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hr.algebra.iis.grpc.TemperatureResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WeatherServiceMethodDescriptorSupplier("GetTemperature"))
              .build();
        }
      }
    }
    return getGetTemperatureMethod;
  }

  private static volatile io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest,
      hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureStreamMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTemperatureStream",
      requestType = hr.algebra.iis.grpc.CityRequest.class,
      responseType = hr.algebra.iis.grpc.TemperatureResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
  public static io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest,
      hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureStreamMethod() {
    io.grpc.MethodDescriptor<hr.algebra.iis.grpc.CityRequest, hr.algebra.iis.grpc.TemperatureResponse> getGetTemperatureStreamMethod;
    if ((getGetTemperatureStreamMethod = WeatherServiceGrpc.getGetTemperatureStreamMethod) == null) {
      synchronized (WeatherServiceGrpc.class) {
        if ((getGetTemperatureStreamMethod = WeatherServiceGrpc.getGetTemperatureStreamMethod) == null) {
          WeatherServiceGrpc.getGetTemperatureStreamMethod = getGetTemperatureStreamMethod =
              io.grpc.MethodDescriptor.<hr.algebra.iis.grpc.CityRequest, hr.algebra.iis.grpc.TemperatureResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTemperatureStream"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hr.algebra.iis.grpc.CityRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  hr.algebra.iis.grpc.TemperatureResponse.getDefaultInstance()))
              .setSchemaDescriptor(new WeatherServiceMethodDescriptorSupplier("GetTemperatureStream"))
              .build();
        }
      }
    }
    return getGetTemperatureStreamMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static WeatherServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeatherServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeatherServiceStub>() {
        @java.lang.Override
        public WeatherServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeatherServiceStub(channel, callOptions);
        }
      };
    return WeatherServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static WeatherServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeatherServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeatherServiceBlockingStub>() {
        @java.lang.Override
        public WeatherServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeatherServiceBlockingStub(channel, callOptions);
        }
      };
    return WeatherServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static WeatherServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<WeatherServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<WeatherServiceFutureStub>() {
        @java.lang.Override
        public WeatherServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new WeatherServiceFutureStub(channel, callOptions);
        }
      };
    return WeatherServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * =============================================
   * KORAK 4: gRPC servis za DHMZ temperaturu
   * =============================================
   * </pre>
   */
  public interface AsyncService {

    /**
     * <pre>
     * Vraća temperaturu za zadani naziv grada
     * </pre>
     */
    default void getTemperature(hr.algebra.iis.grpc.CityRequest request,
        io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTemperatureMethod(), responseObserver);
    }

    /**
     * <pre>
     * Server-streaming: vraća sve gradove koji sadrže traženi pojam
     * </pre>
     */
    default void getTemperatureStream(hr.algebra.iis.grpc.CityRequest request,
        io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTemperatureStreamMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service WeatherService.
   * <pre>
   * =============================================
   * KORAK 4: gRPC servis za DHMZ temperaturu
   * =============================================
   * </pre>
   */
  public static abstract class WeatherServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return WeatherServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service WeatherService.
   * <pre>
   * =============================================
   * KORAK 4: gRPC servis za DHMZ temperaturu
   * =============================================
   * </pre>
   */
  public static final class WeatherServiceStub
      extends io.grpc.stub.AbstractAsyncStub<WeatherServiceStub> {
    private WeatherServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeatherServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeatherServiceStub(channel, callOptions);
    }

    /**
     * <pre>
     * Vraća temperaturu za zadani naziv grada
     * </pre>
     */
    public void getTemperature(hr.algebra.iis.grpc.CityRequest request,
        io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTemperatureMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Server-streaming: vraća sve gradove koji sadrže traženi pojam
     * </pre>
     */
    public void getTemperatureStream(hr.algebra.iis.grpc.CityRequest request,
        io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncServerStreamingCall(
          getChannel().newCall(getGetTemperatureStreamMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service WeatherService.
   * <pre>
   * =============================================
   * KORAK 4: gRPC servis za DHMZ temperaturu
   * =============================================
   * </pre>
   */
  public static final class WeatherServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<WeatherServiceBlockingStub> {
    private WeatherServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeatherServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeatherServiceBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Vraća temperaturu za zadani naziv grada
     * </pre>
     */
    public hr.algebra.iis.grpc.TemperatureResponse getTemperature(hr.algebra.iis.grpc.CityRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTemperatureMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Server-streaming: vraća sve gradove koji sadrže traženi pojam
     * </pre>
     */
    public java.util.Iterator<hr.algebra.iis.grpc.TemperatureResponse> getTemperatureStream(
        hr.algebra.iis.grpc.CityRequest request) {
      return io.grpc.stub.ClientCalls.blockingServerStreamingCall(
          getChannel(), getGetTemperatureStreamMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service WeatherService.
   * <pre>
   * =============================================
   * KORAK 4: gRPC servis za DHMZ temperaturu
   * =============================================
   * </pre>
   */
  public static final class WeatherServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<WeatherServiceFutureStub> {
    private WeatherServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected WeatherServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new WeatherServiceFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Vraća temperaturu za zadani naziv grada
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<hr.algebra.iis.grpc.TemperatureResponse> getTemperature(
        hr.algebra.iis.grpc.CityRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTemperatureMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_TEMPERATURE = 0;
  private static final int METHODID_GET_TEMPERATURE_STREAM = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_TEMPERATURE:
          serviceImpl.getTemperature((hr.algebra.iis.grpc.CityRequest) request,
              (io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse>) responseObserver);
          break;
        case METHODID_GET_TEMPERATURE_STREAM:
          serviceImpl.getTemperatureStream((hr.algebra.iis.grpc.CityRequest) request,
              (io.grpc.stub.StreamObserver<hr.algebra.iis.grpc.TemperatureResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetTemperatureMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              hr.algebra.iis.grpc.CityRequest,
              hr.algebra.iis.grpc.TemperatureResponse>(
                service, METHODID_GET_TEMPERATURE)))
        .addMethod(
          getGetTemperatureStreamMethod(),
          io.grpc.stub.ServerCalls.asyncServerStreamingCall(
            new MethodHandlers<
              hr.algebra.iis.grpc.CityRequest,
              hr.algebra.iis.grpc.TemperatureResponse>(
                service, METHODID_GET_TEMPERATURE_STREAM)))
        .build();
  }

  private static abstract class WeatherServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    WeatherServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return hr.algebra.iis.grpc.WeatherProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("WeatherService");
    }
  }

  private static final class WeatherServiceFileDescriptorSupplier
      extends WeatherServiceBaseDescriptorSupplier {
    WeatherServiceFileDescriptorSupplier() {}
  }

  private static final class WeatherServiceMethodDescriptorSupplier
      extends WeatherServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    WeatherServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (WeatherServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new WeatherServiceFileDescriptorSupplier())
              .addMethod(getGetTemperatureMethod())
              .addMethod(getGetTemperatureStreamMethod())
              .build();
        }
      }
    }
    return result;
  }
}
