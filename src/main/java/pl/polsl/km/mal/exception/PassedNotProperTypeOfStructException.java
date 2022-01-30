package pl.polsl.km.mal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
@ResponseBody
public class PassedNotProperTypeOfStructException extends RuntimeException
{
	public PassedNotProperTypeOfStructException(final String msg){
		super(msg);
	}
}
