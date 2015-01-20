#ifdef NEWAPI
#define BUFFER_TYPE Local<Value>
#define BUFFER_TO_HANDLE(ARG) (ARG)
#define RETURN_TYPE void
#define RETURN(v) { args.GetReturnValue().Set(v); return; }
#define ARGUMENTS v8::FunctionCallbackInfo<Value>
#define LOCAL_VALUE(h) (h)
#define FUNCTION_TO_PERSIST(pfn, fn) pfn.Reset(Isolate::GetCurrent(), fn)
#define FUNCTION_TO_HANDLE(pfn)
Local<Function>::New(Isolate::GetCurrent(), pfn)
#else
#define BUFFER_TYPE Buffer*
#define BUFFER_TO_HANDLE(ARG) Local<Value>::New((ARG)->handle_)
#define RETURN_TYPE Handle<Value>
#define RETURN(v) return scope.Close(v)
#define ARGUMENTS Arguments
#define LOCAL_VALUE(h) Local<Value>(*h)
#define FUNCTION_TO_PERSIST(pfn, fn) pfn = Persistent<Function>::New(fn)
#define FUNCTION_TO_HANDLE(pfn) Local<Function>::New(pfn)
#endif