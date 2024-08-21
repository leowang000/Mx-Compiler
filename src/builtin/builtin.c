#define bool _Bool

typedef unsigned size_t;

void *malloc(size_t n);
void *memcpy(void *dest, const void *src, size_t n);
int printf(const char *pattern, ...);
int scanf(const char *pattern, ...);
int sprintf(char *dest, const char *pattern, ...);
int sscanf(const char *src, const char *pattern, ...);
int strcmp(const char *str1, const char *str2);
char *strcpy(char *dest, const char *src);
size_t strlen(const char *str);

void print(const char *str) {
  printf("%s", str);
}

void println(const char *str) {
  printf("%s\n", str);
}

void printInt(int n) {
  printf("%d", n);
}

void printlnInt(int n) {
  printf("%d\n", n);
}

char *getString() {
  char *res = (char *) malloc(1 << 10);
  scanf("%s", res);
  return res;
}

int getInt() {
  int res;
  scanf("%d", &res);
  return res;
}

char *toString(int i) {
  char *res = (char *) malloc(12);
  sprintf(res, "%d", i);
  return res;
}

void *__array_copy(void *arr, size_t type_size, size_t dim) {
  if (*((size_t *) arr) == 0) {
    return 0;
  }
  size_t array_size = ((size_t *) arr)[-1];
  if (dim == 1) {
    size_t *res = (size_t *) malloc(type_size * array_size + 4);
    return res + 1;
  }
  size_t *res = (size_t *) malloc(4 * array_size + 4);
  res[0] = array_size;
  for (int i = 0; i < array_size; i++) {
    if (((size_t *) arr)[i] == 0) {
      res[i + 1] = 0;
    }
    else {
      res[i + 1] = (size_t) __array_copy((void *) ((size_t *) arr)[i], type_size, dim - 1);
    }
  }
  return res + 1;
}

int __array_size(void *arr) {
  return ((size_t *) arr)[-1];
}

int __string_length(const char *str) {
  return strlen(str);
}

char *__string_substring(const char *str, int left, int right) {
  char *res = (char *) malloc(right - left + 1);
  strcpy(res, str);
  res[right - left] = '\0';
  return res;
}

int __string_parseInt(const char *str) {
  int res;
  sscanf(str, "%d", &res);
  return res;
}

int __string_ord(const char *str, int pos) {
  return str[pos];
}

char *__builtin_bool_to_string(bool value) {
  return value ? "true" : "false";
}

void *__builtin_malloc_array(size_t type_size, size_t array_size) {
  size_t *res = (size_t *) malloc(type_size * array_size + 4);
  res[0] = array_size;
  return res + 1;
}

char *__builtin_string_cat(const char *str1, const char *str2) {
  size_t len1 = strlen(str1), len2 = strlen(str2);
  char *res = (char *) malloc(len1 + len2 + 1);
  strcpy(res, str1);
  strcpy(res + len1, str2);
  res[len1 + len2] = '\0';
  return res;
}

bool __builtin_string_eq(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) == 0;
}

bool __builtin_string_ne(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) != 0;
}

bool __builtin_string_le(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) < 0;
}

bool __builtin_string_ge(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) > 0;
}

bool __builtin_string_leq(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) <= 0;
}

bool __builtin_string_geq(const char *lhs, const char *rhs) {
  return strcmp(lhs, rhs) >= 0;
}