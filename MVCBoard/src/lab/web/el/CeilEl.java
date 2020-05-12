package lab.web.el;

public class CeilEl {
	
	//커스텀 함수 : EL표현식 안에서 내가 원하는 메서드를 만들어 쓰는 것
	//EL표현식은 static메서드만 호출 가능
	public static double pageCeil(double num) {
		return Math.ceil(num);
	}

}
