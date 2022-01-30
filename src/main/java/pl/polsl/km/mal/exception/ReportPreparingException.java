package pl.polsl.km.mal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@ResponseBody
public class ReportPreparingException extends RuntimeException
{
	public ReportPreparingException(final String msg){
		super(msg);
	}
}
