这个题很好，运用了 Decorator 的思想。

Decorator模式有什么好处？它实际上把核心功能和附加功能给分开了。
其实很类似UI里的 wrapper. 但是UI操作起来更容易。

```txt
装饰器和桥接的目的都是降低继承中衍生的子类的数量，

桥接是通过把一个组件及其子类作为另一总体的字段引用实现功能组合，也可以用多个组件来拼合总体

装饰器则在大类下创建一个装饰器的子族，不管是主要部件还是装饰器都隶属于这个大类，所以装饰器可以不断嵌套

看起来桥接和装饰器的使用都是层层调用，但是两者的功能不同，桥接的子类是负责总体的局部功能，是构成性的

而装饰器则是对已经具有了完整功能的总体进行修饰，是附加性的。

相当于雪中送炭和锦上添花的区别。
```

这个题其实适用于所有的 filter 类。

一开始我们就简单的 实现 interface 接口。
然后我们的觉得，用一个 Decorator 搞一下这个与或非就非常好。
这里我觉得与或非是最关键的地方，因为彻底让代码变得易于组织。
除此之外，可以用简单工厂生成不同的Filter (新建一个 SimpleFileFilterFactory class)
然后相应的，增加一个新的枚举类。因为简单工厂不是一个特别好的例子，所以不一定用它。

参考一下廖雪峰 
https://liaoxuefeng.com/books/java/design-patterns/structural/decorator/index.html

如果每个都加子类，子类会爆炸性的增长。

```java

// "static void main" must be defined in a public class.
class File {
    String fileName;
    String extension;
    Integer size;
    public File(String fileName, String extension, Integer size) {
        this.fileName = fileName;
        this.extension = extension;
        this.size = size;
    }
    
    @Override
    public String toString() {
        return fileName + " " + extension + " " + size;
    }
}

interface FileFilter {
    public boolean accept(File file);    
}

class FileNameFilter implements FileFilter {
    private String pattern;
    public FileNameFilter(String pattern) {
        this.pattern = pattern;
    }
    
    @Override
    public boolean accept(File file) {
        return file.fileName.contains(pattern);
    }
}

class ExtensionFilter implements FileFilter {
    private String extension;
    public ExtensionFilter(String extension) {
        this.extension = extension;
    }
    
    @Override
    public boolean accept(File file) {
        return file.extension.equals(this.extension);
    }
}

abstract class DecoratorFilter implements FileFilter {
    protected FileFilter firstFilter;
    protected DecoratorFilter(FileFilter filter) {
        this.firstFilter = filter;
    }
    // @Override
    // public boolean accept(File file) {
    //     return firstFilter.accept(file);
    // }
    // Abstract class 不用 实现一切
}

class AndFilter extends DecoratorFilter {
    private FileFilter secondFilter;
    
    public AndFilter(FileFilter filter1, FileFilter filter2) {
        super(filter1);
        this.secondFilter = filter2;
    }
    
    @Override
    public boolean accept(File file) {
        return firstFilter.accept(file) && secondFilter.accept(file);
    }
}

//TODO: You can create OrFilter, NotFilter similarly




public class Main {

    public static void main(String[] args) {
        File file1 = new File("movie-123", "mp4", 5000);
        File file2 = new File("photo-123", "jpg", 2000);
        File file3 = new File("music-123", "mp4", 3000);
        List<File> fileList = Arrays.asList(new File[]{file1, file2, file3});
        
        FileNameFilter nameFilter1 = new FileNameFilter("m");
        FileNameFilter nameFilter2 = new FileNameFilter("123");      
        ExtensionFilter extensionFilter1 = new ExtensionFilter("mp4");      
        FileFilter compositeFilter = new AndFilter(nameFilter1, new AndFilter(nameFilter2, extensionFilter1));
        
        List<File> newList = filterFiles(fileList, compositeFilter);
        for (File file : newList) {
            System.out.println(file.toString());
        }
    }
    
    private static List<File> filterFiles(List<File> files, FileFilter filter) {
        List<File> newFiles = new ArrayList<>();
        for (File file : files) {
            if (filter.accept(file)) {
                newFiles.add(file);
            }
        }
        return newFiles;
    }
}


// Finished in 67 ms
// movie-123 mp4 5000
// music-123 mp4 3000

```

补充，可以写个 enum 给子类，让filter实现多功能。

```java
// You can consider put this inside the group or outside.
// if define inside the class, call it an option
enum NumericalRelation {
    GREATER,
    GREATER_EQUAL,
    EQUAL,
    NOT_EQUAL,
    LESS_EQUAL,
    LESS;
}

class FileSizeFilter implements FileFilter {
    private int size;
    private NumericalRelation relation;

    public FileSizeFilter(NumericalRelation relation, int size) {
        this.relation = relation;
        this.size = size;
    }

    @Override
    public boolean accept(File file) {
        // add if file == null case would be better
        switch (relation) {
            case GREATER:
                return file.length() > size;
            case GREATER_EQUAL:
                return file.length() >= size;
            // Add other cases if needed
            default:
                throw new IllegalStateException("Unexpected value: " + relation);
        }
    }
}

```