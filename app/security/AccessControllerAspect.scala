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
        println(Authentication.getRequestURI)
        play.mvc.Results.redirect(routes.Authentication.loginToTargetURL(Authentication.getRequestURI));
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
          play.mvc.Results.unauthorized("Access Denied")
      }
    }
    catch {
      case ex: NoSuchMethodException => play.mvc.Results.badRequest("The target method cannot access session")
    }
  }

  @Around("execution(@security.RestrictTOUser * *.*(..))")
  def restrictToUser(pjp: ProceedingJoinPoint): Object = {
    val myClass: Class[_] = pjp.getStaticPart().getSignature().getDeclaringType();

    try {
      val user = Authentication.getLocalUser
      if (user == null) {
        println(Authentication.getRequestURI)
        play.mvc.Results.redirect(routes.Authentication.loginToTargetURL(Authentication.getRequestURI));
      } else {
        val signature = pjp.getSignature.asInstanceOf[MethodSignature]
        val restrictTo = signature.getMethod.getAnnotation(classOf[RestrictTOUser]).value()
        if (user.email == restrictTo)
          pjp.proceed()
        else
          play.mvc.Results.unauthorized("Access Denied")
      }
    }
    catch {
      case ex: NoSuchMethodException => play.mvc.Results.badRequest("The target method cannot access session")
    }
  }

  //play.api.mvc.Controller
}