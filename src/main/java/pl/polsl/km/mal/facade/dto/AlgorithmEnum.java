package pl.polsl.km.mal.facade.dto;

public enum AlgorithmEnum
{
	SPARE, TRIGG, RENEW,LIST;

	public static boolean contains(String algorithm){
		for (AlgorithmEnum c : AlgorithmEnum.values()) {
			if (c.name().equals(algorithm) && !algorithm.equals(LIST.name())) {
				return true;
			}
		}
		return false;
	}
}
