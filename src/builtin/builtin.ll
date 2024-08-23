; ModuleID = 'builtin.c'
source_filename = "builtin.c"
target datalayout = "e-m:e-p:32:32-i64:64-n32-S128"
target triple = "riscv32-unknown-unknown-elf"

@.str = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@.str.2 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.3 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@.str.4 = private unnamed_addr constant [5 x i8] c"true\00", align 1
@.str.5 = private unnamed_addr constant [6 x i8] c"false\00", align 1

; Function Attrs: nounwind
define dso_local void @print(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str, ptr noundef %0) #5
  ret void
}

declare dso_local i32 @printf(ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local void @println(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.1, ptr noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local void @printInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.2, i32 noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local void @printlnInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.3, i32 noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local noundef ptr @getString() local_unnamed_addr #0 {
  %1 = tail call ptr @malloc(i32 noundef 1024) #5
  %2 = tail call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str, ptr noundef %1) #5
  ret ptr %1
}

; Function Attrs: mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite)
declare void @llvm.lifetime.start.p0(i64 immarg, ptr nocapture) #2

declare dso_local ptr @malloc(i32 noundef) local_unnamed_addr #1

declare dso_local i32 @scanf(ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite)
declare void @llvm.lifetime.end.p0(i64 immarg, ptr nocapture) #2

; Function Attrs: nounwind
define dso_local i32 @getInt() local_unnamed_addr #0 {
  %1 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %1) #6
  %2 = call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str.2, ptr noundef nonnull %1) #5
  %3 = load i32, ptr %1, align 4, !tbaa !6
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %1) #6
  ret i32 %3
}

; Function Attrs: nounwind
define dso_local noundef ptr @toString(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call ptr @malloc(i32 noundef 12) #5
  %3 = tail call i32 (ptr, ptr, ...) @sprintf(ptr noundef %2, ptr noundef nonnull @.str.2, i32 noundef %0) #5
  ret ptr %2
}

declare dso_local i32 @sprintf(ptr noundef, ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local ptr @array.copy(ptr nocapture noundef readonly %0, i32 noundef %1, i32 noundef %2) local_unnamed_addr #0 {
  %4 = load i32, ptr %0, align 4, !tbaa !6
  %5 = icmp eq i32 %4, 0
  br i1 %5, label %39, label %6

6:                                                ; preds = %3
  %7 = getelementptr inbounds i8, ptr %0, i32 -4
  %8 = load i32, ptr %7, align 4, !tbaa !6
  %9 = icmp eq i32 %2, 1
  br i1 %9, label %10, label %14

10:                                               ; preds = %6
  %11 = mul i32 %8, %1
  %12 = add i32 %11, 4
  %13 = tail call ptr @malloc(i32 noundef %12) #5
  br label %36

14:                                               ; preds = %6
  %15 = shl i32 %8, 2
  %16 = add i32 %15, 4
  %17 = tail call ptr @malloc(i32 noundef %16) #5
  store i32 %8, ptr %17, align 4, !tbaa !6
  %18 = getelementptr inbounds i8, ptr %17, i32 4
  %19 = icmp eq i32 %8, 0
  br i1 %19, label %36, label %20

20:                                               ; preds = %14
  %21 = add i32 %2, -1
  br label %22

22:                                               ; preds = %20, %31
  %23 = phi i32 [ 0, %20 ], [ %34, %31 ]
  %24 = getelementptr inbounds i32, ptr %0, i32 %23
  %25 = load i32, ptr %24, align 4, !tbaa !6
  %26 = icmp eq i32 %25, 0
  br i1 %26, label %31, label %27

27:                                               ; preds = %22
  %28 = inttoptr i32 %25 to ptr
  %29 = tail call ptr @array.copy(ptr noundef nonnull %28, i32 noundef %1, i32 noundef %21) #7
  %30 = ptrtoint ptr %29 to i32
  br label %31

31:                                               ; preds = %22, %27
  %32 = phi i32 [ %30, %27 ], [ 0, %22 ]
  %33 = getelementptr inbounds i32, ptr %18, i32 %23
  store i32 %32, ptr %33, align 4, !tbaa !6
  %34 = add nuw i32 %23, 1
  %35 = icmp eq i32 %34, %8
  br i1 %35, label %36, label %22, !llvm.loop !10

36:                                               ; preds = %31, %14, %10
  %37 = phi ptr [ %13, %10 ], [ %17, %14 ], [ %17, %31 ]
  %38 = getelementptr inbounds i8, ptr %37, i32 4
  br label %39

39:                                               ; preds = %3, %36
  %40 = phi ptr [ %38, %36 ], [ null, %3 ]
  ret ptr %40
}

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read)
define dso_local i32 @array.size(ptr nocapture noundef readonly %0) local_unnamed_addr #3 {
  %2 = getelementptr inbounds i8, ptr %0, i32 -4
  %3 = load i32, ptr %2, align 4, !tbaa !6
  ret i32 %3
}

; Function Attrs: nounwind
define dso_local i32 @string.length(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 @strlen(ptr noundef %0) #5
  ret i32 %2
}

declare dso_local i32 @strlen(ptr noundef) local_unnamed_addr #1

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read)
define dso_local range(i32 0, 256) i32 @string.ord(ptr nocapture noundef readonly %0, i32 noundef %1) local_unnamed_addr #3 {
  %3 = getelementptr inbounds i8, ptr %0, i32 %1
  %4 = load i8, ptr %3, align 1, !tbaa !12
  %5 = zext i8 %4 to i32
  ret i32 %5
}

; Function Attrs: nounwind
define dso_local i32 @string.parseInt(ptr noundef %0) local_unnamed_addr #0 {
  %2 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %2) #6
  %3 = call i32 (ptr, ptr, ...) @sscanf(ptr noundef %0, ptr noundef nonnull @.str.2, ptr noundef nonnull %2) #5
  %4 = load i32, ptr %2, align 4, !tbaa !6
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %2) #6
  ret i32 %4
}

declare dso_local i32 @sscanf(ptr noundef, ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local noundef ptr @string.substring(ptr noundef %0, i32 noundef %1, i32 noundef %2) local_unnamed_addr #0 {
  %4 = sub nsw i32 %2, %1
  %5 = add nsw i32 %4, 1
  %6 = tail call ptr @malloc(i32 noundef %5) #5
  %7 = tail call ptr @strcpy(ptr noundef %6, ptr noundef %0) #5
  %8 = getelementptr inbounds i8, ptr %6, i32 %4
  store i8 0, ptr %8, align 1, !tbaa !12
  ret ptr %6
}

declare dso_local ptr @strcpy(ptr noundef, ptr noundef) local_unnamed_addr #1

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(none)
define dso_local noundef nonnull ptr @builtin.bool_to_string(i1 noundef zeroext %0) local_unnamed_addr #4 {
  %2 = select i1 %0, ptr @.str.4, ptr @.str.5
  ret ptr %2
}

; Function Attrs: nounwind
define dso_local ptr @builtin.calloc(i32 noundef %0, i32 noundef %1) local_unnamed_addr #0 {
  %3 = tail call ptr @calloc(i32 noundef %0, i32 noundef %1) #5
  ret ptr %3
}

declare dso_local ptr @calloc(i32 noundef, i32 noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local ptr @builtin.malloc(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call ptr @malloc(i32 noundef %0) #5
  ret ptr %2
}

; Function Attrs: nounwind
define dso_local nonnull ptr @builtin.malloc_array(i32 noundef %0, i32 noundef %1) local_unnamed_addr #0 {
  %3 = mul i32 %1, %0
  %4 = add i32 %3, 4
  %5 = tail call ptr @malloc(i32 noundef %4) #5
  store i32 %1, ptr %5, align 4, !tbaa !6
  %6 = getelementptr inbounds i8, ptr %5, i32 4
  ret ptr %6
}

; Function Attrs: nounwind
define dso_local void @builtin.strcat(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call ptr @strcat(ptr noundef %0, ptr noundef %1) #5
  ret void
}

declare dso_local ptr @strcat(ptr noundef, ptr noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local noundef ptr @builtin.string_add(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strlen(ptr noundef %0) #5
  %4 = tail call i32 @strlen(ptr noundef %1) #5
  %5 = add i32 %4, %3
  %6 = add i32 %5, 1
  %7 = tail call ptr @malloc(i32 noundef %6) #5
  %8 = tail call ptr @strcpy(ptr noundef %7, ptr noundef %0) #5
  %9 = getelementptr inbounds i8, ptr %7, i32 %3
  %10 = tail call ptr @strcpy(ptr noundef %9, ptr noundef %1) #5
  %11 = getelementptr inbounds i8, ptr %7, i32 %5
  store i8 0, ptr %11, align 1, !tbaa !12
  ret ptr %7
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_eq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp eq i32 %3, 0
  ret i1 %4
}

declare dso_local i32 @strcmp(ptr noundef, ptr noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_ge(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp sgt i32 %3, 0
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_geq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp sgt i32 %3, -1
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_le(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp slt i32 %3, 0
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_leq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp slt i32 %3, 1
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_ne(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp ne i32 %3, 0
  ret i1 %4
}

attributes #0 = { nounwind "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #1 = { "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #2 = { mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite) }
attributes #3 = { mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read) "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #4 = { mustprogress nofree norecurse nosync nounwind willreturn memory(none) "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #5 = { nobuiltin nounwind "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" }
attributes #6 = { nounwind }
attributes #7 = { "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcat" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" }

!llvm.module.flags = !{!0, !1, !2, !4}
!llvm.ident = !{!5}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{i32 1, !"target-abi", !"ilp32"}
!2 = !{i32 6, !"riscv-isa", !3}
!3 = !{!"rv32i2p1_m2p0_a2p1_c2p0_zmmul1p0"}
!4 = !{i32 8, !"SmallDataLimit", i32 8}
!5 = !{!"Ubuntu clang version 20.0.0 (++20240817032242+76161451f5ca-1~exp1~20240817152407.1871)"}
!6 = !{!7, !7, i64 0}
!7 = !{!"int", !8, i64 0}
!8 = !{!"omnipotent char", !9, i64 0}
!9 = !{!"Simple C/C++ TBAA"}
!10 = distinct !{!10, !11}
!11 = !{!"llvm.loop.mustprogress"}
!12 = !{!8, !8, i64 0}
