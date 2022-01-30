package pl.polsl.km.mal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
@ResponseBody
public class UnableToRunStreamDatabase extends RuntimeException
{
	public UnableToRunStreamDatabase(final String msg){
		super(msg);
	}
}
