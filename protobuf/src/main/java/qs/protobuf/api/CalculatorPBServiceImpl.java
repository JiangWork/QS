package qs.protobuf.api;

import qs.protobuf.proto.CalculatorMsg.RequestProto;
import qs.protobuf.proto.CalculatorMsg.ResponseProto;

import com.google.protobuf.RpcController;
import com.google.protobuf.ServiceException;

public class CalculatorPBServiceImpl implements CalculatorPB {

	public Calculator real;  
    
    public CalculatorPBServiceImpl(Calculator impl){  
        this.real = impl;  
    }  
    
	@Override
	public ResponseProto add(RpcController controller, RequestProto request)
			throws ServiceException {
		 // TODO Auto-generated method stub  
        ResponseProto.Builder build = ResponseProto.newBuilder();  
        int add1 = request.getNum1();  
        int add2 = request.getNum2();  
        int sum = real.add(add1, add2);  
        ResponseProto result = null;  
        build.setResult(sum);  
        result = build.build();  
        return result; 
	}

	@Override
	public ResponseProto minus(RpcController controller, RequestProto request)
			throws ServiceException {
		// TODO Auto-generated method stub
	        ResponseProto.Builder build = ResponseProto.newBuilder();  
	        int add1 = request.getNum1();  
	        int add2 = request.getNum2();  
	        int sum = real.minus(add1, add2);  
	        ResponseProto result = null;  
	        build.setResult(sum);  
	        result = build.build();  
	        return result;  
	}

}
