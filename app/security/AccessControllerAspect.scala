package security

import controllers.{routes, Authentication}
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}
import org.aspectj.lang.reflect.MethodSignature

@Aspect
class AccessControllerAspect {
  @Around("execution(@security.AllowedRoles * *.*(..))")
  def accessControl(pjp: ProceedingJoinPoint): Object = {
    val myClass: Class[_] = pjp.getStaticPart().getSignature().getDeclaringType();

    try {

      val user = Authentication.getLocalUser
      if (user == null) {
        play.mvc.Results.redirect(routes.Authentication.login);
      } else {
        val signature = pjp.getSignature.asInstanceOf[MethodSignature]
        val allowedRoles = signature.getMethod.getAnnotation(classOf[AllowedRoles]).value()
        var allow = false
        for (role <- allowedRoles) {
          if (user.hasRole(role))
            allow = true; //or
        }
        if (allow)
          pjp.proceed()
        else
          play.mvc.Results.badRequest("Access Denied")
      }
    }
    catch {
      case ex: NoSuchMethodException => play.mvc.Results.badRequest("The target method cannot access session")
    }
  }

  //play.api.mvc.Controller
}