typedef struct {
    char cNames[50][10]; //一维数组指针
} Name;

Name classes[7]; // Name (* classes) [7] 结构体数组
classes[0].cNames[0][0] = 'A';

char cNamesFromClasses[7][50][10]; // char (* cNamesFromClasses) [50][10] 二维数组指针
