package inspection
//
//import com.intellij.codeInsight.daemon.GroupNames
//import com.intellij.codeInspection.ProblemsHolder
//
////import com.intellij.codeInspection.ProblemsHolder
////import org.jetbrains.kotlin.com.intellij.codeInspection.ProblemsHolder
//import com.intellij.psi.PsiElement
//import org.jetbrains.kotlin.com.intellij.psi.PsiElementVisitor
//import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
//import org.jetbrains.kotlin.psi.KtNamedDeclaration
//import org.jetbrains.kotlin.psi.KtVisitorVoid
//import org.jetbrains.kotlin.psi.namedDeclarationVisitor
//
//class CamelcaseInspection : AbstractKotlinInspection() {
//
//    override fun getDisplayName(): String {
//        return "Use CamelCase naming"
//    }
//
//    override fun getGroupDisplayName(): String {
//        return GroupNames.STYLE_GROUP_NAME
//    }
//
//    override fun getShortName(): String {
//        return "Camelcase"
//    }
//
//    override fun buildVisitor(holder: ProblemsHolder,
//                              isOnTheFly: Boolean): PsiElementVisitor {
//        return object: KtVisitorVoid() {
//            override fun visitNamedDeclaration(declaration: KtNamedDeclaration) {
////                declaration.name.let { declaredName ->
//                    if (declaration.name?.isDefinedCamelCase() == false) {
//                        System.out.println("Non CamelCase Name Detected for ${declaration.name}")
//                        holder.registerProblem(
//                            declaration.nameIdentifier as PsiElement,
//                            "Please use CamelCase for #ref #loc"
//                        )
//                    }
////                }
//            }
//        }
////        return namedDeclarationVisitor { declaredName ->
////            if (declaredName.name?.isDefinedCamelCase() == false) {
////                System.out.println("Non CamelCase Name Detected for ${declaredName.name}")
////                holder.registerProblem(
////                    declaredName.nameIdentifier as PsiElement,
////                    "Please use CamelCase for #ref #loc")
////            }
////        }
//    }
//
////    override fun buildVisitor(
////        holder: ProblemsHolder,
////        isOnTheFly: Boolean
////    ): PsiElementVisitor {
////        return object: KtVisitorVoid() {
////            override fun visitNamedFunction(function: KtNamedFunction) {
////                function.name?.let {
////                    if (it.length > MAX_LENGTH) {
////                        holder.registerProblem(
////                            function,
////                            "Function name ${function.name} is longer than allowed $MAX_LENGTH"
////                        )
////                    }
////                }
////            }
////        }
////    }
//
//    private fun String.isDefinedCamelCase(): Boolean {
//        val toCharArray = toCharArray()
//        return toCharArray
//            .mapIndexed { index, current ->
//                current to toCharArray.getOrNull(index + 1) }
//            .none { it.first.isUpperCase() &&
//                    it.second?.isUpperCase() ?: false }
//    }
//
//    override fun isEnabledByDefault(): Boolean {
//        return true
//    }
//}



import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtVisitorVoid

private const val MAX_LENGTH = 20

class FunctionNameLengthInspection : AbstractKotlinInspection() {

    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object: KtVisitorVoid() {
            override fun visitNamedFunction(function: KtNamedFunction) {
                function.name?.let {
                    if (it.length > MAX_LENGTH) {
                        holder.registerProblem(
                            function,
                            "Function name ${function.name} is longer than allowed $MAX_LENGTH"
                        )
                    }
                }
            }
        }
    }

}