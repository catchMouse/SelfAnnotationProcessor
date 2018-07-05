package com.example.szx.apt;

import com.example.szx.anno.BindSelfView;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;

//当前module主要用于解析CLASS类型的注解，并生成对应的结果
//那么当前类就会被当成默认的注解处理器--- @AutoService(Processor.class) 注册动作
//@SupportedAnnotationTypes(path), must be abs path
@AutoService(Processor.class)
@SupportedAnnotationTypes("com.example.szx.anno.BindSelfView")
public class MyAnnoProcessor extends AbstractProcessor {
    /* tools that help to create source,class or property file*/
    Filer mFiler = null;

    /*
    tools that help to get element-info,
    such as javadoc desc about some element, the package of this element
    */
    Elements elementsUtils = null;

    /**
     *
     * @param processingEnvironment
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        System.out.println("MyAnnoProcessor init is invoked.processingEnvironment="+processingEnvironment);
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        elementsUtils = processingEnvironment.getElementUtils();

    }

    /**
     target such as:
     public class MainActivity_BindView extends Activity {
        TextView textView;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            textView = (TextView)findViewById(R.id.id_textview)
        }
    }
     * @param set set of TypeElement  --- size=1   BindSelfView
     * @param roundEnvironment   contains annotation decorate class
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        System.out.println("MyAnnoProcessor process is invoked.");
        StringBuilder sb = new StringBuilder();
        for (TypeElement element : set) {
            String des = "element:"+element.getSimpleName()+ " extends " + element.getSuperclass().getClass().getSimpleName();
            sb.append(des+"===");
        }
        sb.append("roundEnvironment:"+roundEnvironment.getClass().getSimpleName());

        //当前module无法访问Android API
        TypeSpec.Builder tb = TypeSpec.classBuilder("MainActivity_BindView").
                addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(String.class, "textView", Modifier.PRIVATE)
                .addJavadoc(sb.toString());  //class


        //addStatement or addCode to add code
        /**
         addCode(""
         + "int total = 0;\n"
         + "for (int i = 0; i < 10; i++) {\n"
         + "  total += i;\n"
         + "}\n")

         control code:  beginControlFlow + endControlFlow
         build.addStatement("int total = 0")
         .beginControlFlow("for (int i = 0; i < 10; i++)")
         .addStatement("total += i")
         .endControlFlow()

         control with var style:
         .addStatement("int result = 1")
         .beginControlFlow("for (int i = " + from + "; i < " + to + "; i++)")
         .addStatement("result = result " + op + " i")
         .endControlFlow()
         .addStatement("return result")
         */
        //ParameterSpec for Parameter   .initializer("$S", "change")
        //FieldSpec for field
        //TypeSpec.interfaceBuilder("HelloWorld") for interface
        //TypeSpec.enumBuilder("Roshambo") .addEnumConstant("SCISSORS") TypeSpec.anonymousClassBuilder("$S", "peace")  for enum

        /** anonymous class
         TypeSpec.anonymousClassBuilder("")
         .addSuperinterface(ParameterizedTypeName.get(Comparator.class, String.class))
         */

        MethodSpec.Builder mb = MethodSpec.methodBuilder("onCreate").returns(TypeName.VOID).
                //addAnnotation(Override.class).
                addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "savedInstanceState")
                ;  //method;

        // $L --> literal value(a value not changed); %s --> String  ;
        // %T --> reference Types; %N --> functionName
        //CodeBlock.builder().add("I ate $L $L", 3, "tacos")
        //CodeBlock.builder().add("I ate $2L $1L", "tacos", 3)


        //CodeBlock.addNamed() + $argumentName:X
        /**
         Map<String, Object> map = new LinkedHashMap<>();
         map.put("food", "tacos");
         map.put("count", 3);
         CodeBlock.builder().addNamed("I ate $count:L $food:L", map)
         */

        //get elements with annotation
        Set<? extends Element> eleSet = roundEnvironment.getElementsAnnotatedWith(BindSelfView.class);

        Set<TypeElement> myset = ElementFilter.typesIn(eleSet);  //a filter to get element wo want   TypeElement means class
        mb.addStatement("int myset = $L", myset.size());   //filter from roundEnvironment
        mb.addCode("int set = $L;\n", set.size());  //method param
        mb.addCode("int eleSet = $L;\n", eleSet.size());   //from roundEnvironment
        Iterator<? extends Element> it = eleSet.iterator();
        while (it.hasNext()) {
            Element ele = it.next();
            if (ele instanceof TypeElement) {
                Annotation anno = ele.getAnnotation(BindSelfView.class);
                BindSelfView view = (BindSelfView)anno;
                String desc = "eleName:"+ele.getSimpleName().toString() +
                        ",annoName:"+anno.annotationType().getClass().getSimpleName() + ",annoValue:"+view.value() + ",kind:"+ele.getKind();

                CodeBlock cb = CodeBlock.builder()
                        .addStatement("System.out.println($S)", desc)
                        .build();
                mb.addCode(cb);
            } else if (ele instanceof VariableElement) {   //means field
                Annotation anno = ele.getAnnotation(BindSelfView.class);
                BindSelfView view = (BindSelfView)anno;
                String desc = "eleName:"+ele.getSimpleName().toString() +
                        ",annoName:"+anno.annotationType().getClass().getSimpleName() + ",annoValue:"+view.value() + ",kind:"+ele.getKind();

                CodeBlock cb = CodeBlock.builder()
                        .addStatement("System.out.println($S)", desc)
                        .build();
                mb.addCode(cb);
            }
        }

        /*for (TypeElement element : myset) {
            Annotation anno = element.getAnnotation(BindSelfView.class);
            BindSelfView view = (BindSelfView)anno;
            String desc = "eleName:"+element.getSimpleName().toString() +
                    ",annoName:"+anno.annotationType().getClass().getSimpleName() + ",annoValue:"+view.value();

            CodeBlock cb = CodeBlock.builder()
                    .addStatement("System.out.println($S)", desc)
                    .build();
            mb.addCode(cb);
        }*/
        /*for (TypeElement element : set) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("SET: eleName:"+element.getSimpleName().toString());
            Annotation anno = element.getAnnotation(BindSelfView.class);
            if (anno != null) {
                BindSelfView view = (BindSelfView) anno;
                sb2.append(",annoName:"+anno.annotationType().getClass().getSimpleName());
                if (view != null) {
                    sb2.append(",annoValue:"+view.value());
                }
            }
            CodeBlock cb = CodeBlock.builder()
                    .addStatement("System.out.println($S)", sb2.toString())
                    .build();
            mb.addCode(cb);
        }*/





        /**
         ClassName usage : ClassName list = ClassName.get("java.util", "List");   //identify any declared class
         ClassName hoverboard = ClassName.get("com.mattel", "Hoverboard");
         TypeName listOfHoverboards = ParameterizedTypeName.get(list, hoverboard);  // List<Hoverboard>
         ParameterizedTypeName.get(List.class, String.class)

         static import:
         ClassName namedBoards = ClassName.get("com.mattel", "Hoverboard", "Boards");
         javaFile.builder(XX)
         .addStaticImport(hoverboard, "createNimbus") //one
         .addStaticImport(namedBoards, "*")  //all
         .addStaticImport(Collections.class, "*")
         */
        //MethodSpec.constructorBuilder() --> generate constructor method
        tb.addMethod(mb.build());   //put method into class

        //put class into java file
        JavaFile jf = JavaFile.builder("com.example.szx.selfannotationprocessor", tb.build())
                .build();
        try {
            jf.writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        System.out.println("MyAnnoProcessor getSupportedAnnotationTypes is invoked.");
        //return super.getSupportedAnnotationTypes();

        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(BindSelfView.class.getCanonicalName());
        return annotataions;

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        System.out.println("MyAnnoProcessor getSupportedSourceVersion is invoked.");
        return SourceVersion.latestSupported();
    }


}
