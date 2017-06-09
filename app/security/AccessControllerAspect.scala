package security

import controllers.{Authentication, routes}
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.{Around, Aspect}
import org.aspectj.lang.reflect.MethodSignature
import play.api.Logger
import play.mvc.Controller

@Aspect
class AccessControllerAspect extends Controller {
  @Around("execution(@security.AllowedRoles * *.*(..))")
  def accessControl(pjp: ProceedingJoinPoint): Object = {

    try {
      val user = Authentication.getLocalUser(play.mvc.Http.Context.current().session())
      if (user == null) {
        Logger.debug(s"LOGIN FAIL redirect ${play.mvc.Http.Context.current().request().uri()})")
        play.mvc.Results.redirect(routes.Authentication.loginToTargetURL(play.mvc.Http.Context.current().request().uri()));
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
    val field = pjp.getTarget.getClass.getDeclaredField("authentication");
    val authentication = field.get(pjp.getTarget).asInstanceOf[Authentication];

    try {
      val user = Authentication.getLocalUser(play.mvc.Http.Context.current().session())
      if (user == null) {
        Logger.debug(s"LOGIN FAIL redirect ${play.mvc.Http.Context.current().request().uri()})")
        play.mvc.Results.redirect(routes.Authentication.loginToTargetURL(play.mvc.Http.Context.current().request().uri()));
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