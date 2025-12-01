typedef struct {
    char cNames[50][10]; //一维数组指针
} Name;

Name classes[7];

char cNamesFromClasses[7][50][10]; // char (* cNamesFromClasses) [50][10] 二维数组指针
