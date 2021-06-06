#include<iostream>
#include"json.hpp"
#include <stdio.h>
#include <string.h>
        #include<time.h>
        #include<math.h>
        #include <sys/stat.h>
        #include <sys/types.h>
        #include<sys/syscall.h>
        #include <sys/mman.h>
        #include <sys/shm.h>
        #include<string.h>
        #include <cstring>
#include <stdlib.h>
        #include <fcntl.h>
        #include <unistd.h>
        #include <stdio.h>
        #include<conio.h>
        #include <errno.h>
        #include <ctype.h>
        #include <cstring>
#include<vector>
#include<sstream>
#include<fstream>
#include<stack>


using namespace std;
using namespace nlohmann;



char currentDir[] = "root";
#define TotalBlock 1000

        const char* directory = "paths_file.txt";
        const char* table_Name = "table_file.txt";
        const char* datablocks = "data_file.txt";


//declaring the block size
 int blockSize = 512;



class File_Table
{
    private:
    Rows<bool, int> tables[1000];	//1000 blocks bool tells if block free and int gives nextone
    public:

    File_Table()
    {
        ifstream read;
        read.open(tableName);
        int next;
        bool val;
        int i = 0;
        if (read.is_open())
        {
            read >> val;
            read >> next;
            tables[i].setting_Value(val);
            tables[i++].setting_Next(next);
            read.close();
        }
        else
        {
            for (int i = 0; i < 1000; i++)
                tables[i].setting_Next(-1);
        }
    }
    void reseting_Next(int i)
    {
        if (i < 1000)
            tables[i].setting_Next(-1);
        else
            throw 1;
    }

    int finding_Free()
    {
        for (int i = 0; i < 1000; i++)
        {
            if (!tables[i].getting_Value())
                return i;
        }
    }

    void setting_Next(int i, int n)
    {
        if (i < 1000)
            tables[i].setting_Next(n);
        else
            throw 1;
    }
    void resetting_Value(int i)
    {
        if (i < 1000)
            tables[i].setting_Value(false);
        else
            throw 1;
    }
    void setting_Value(int i)
    {
        if (i < 1000)
            tables[i].setting_Value(true);
        else
            throw 1;
    }

    int getting_Next(int i)
    {
        return tables[i].getting_Next();
    }


    void writing_File()
    {
        ofstream write;
        write.open(tableName);
        for (int i = 0; i < 1000; i++)
        {
            cout << tables[i].getting_Value() << " " << tables[i].getting_Next() << "\n";
        }
        write.close();
    }
	~

    File_Table()
    {
        ofstream write;
        write.open(tableName);
        for (int i = 0; i < 1000; i++)
        {
            cout << tables[i].getting_Value() << " " << tables[i].getting_Next() << "\n";
        }
        write.close();
    }
};


template<class T, class C>
class Rows
{
    private:
    T value;
    C next;
    public:
    Rows(T f = 0, C c = 0)
    {
        value = f;
        next = c;
    }
    T getting_Value()
    {
        return value;
    }
    C getting_Next()
    {
        return next;
    }
    void setting_Value(T s)
    {
        value = s;
    }
    void setting_Next(C c)
    {
        next = c;
    }
};

class Directory_Structure
{
    public:
    json root;
    string File;
    Rows<char*, json*>* cwd;
    Directory_Structure()
    {
        ifstream read;
        read.open(directory);
        if (read.is_open())
        {
            stringstream fileInStream;
            while (read.peek() != EOF)
            {
                fileInStream << (char)read.get();
            }
            this->File = fileInStream.str();
            read.close();
            root = json::parse(File);
        }
        cwd = new Rows<char *, json*>((char*)"root", &root);

    }
    Directory_Structure()
    {
        ofstream write;
        write.open(directory);
        write << root.dump(4);
        write.close();
    }


    void saving_State()
    {
        ofstream write;
        write.open(directory);
        write << root.dump(4);
        write.close();
    }

	~Directory_Structure()
{
    cout<<"Destructor has been called"<<endl;
}

};

class File_System
{
    private:
    File_Table F;
    Directory_Structure paths;
    stack<Rows<char*, json*>*> track;
    char name[80];

    public:
    File_System()
    {
        ifstream fin;
        fin.open(datablocks);
        if (fin.is_open())
            fin.close();
        else {
            char buffer[80];
            cout << "Enter Block Size\n";
            cin >> blockSize;
            cout << "Name your Drive\n";
            cin.getline(name, 80);
            cout << "File system successfully mounted\n";
            ofstream write(datablocks);
            write.close();
        }
    }
   //making the directory
    void mkdir(string arguments)
    {
        json* pwd = (paths.cwd->getNext());
        if (pwd->find(arguments) == pwd->end())
        {
            (*pwd)[arguments] = nullptr;
            this->paths.saving_State();
        }
        else
            cout << "duplicate files are not allowed\n";

    }
    //list the files
    void ls()
    {
        json* pwd = paths.cwd->getNext();
        for (auto obj = pwd->begin(); obj != pwd->end(); ++obj)
            cout << obj.key() << "\n";
    }
    //fetching the data
    void fetch(string arguments)
    {
        json* pwd = (paths.cwd->getNext());
        if (pwd->find(arguments) == pwd->end())
        {
            ifstream read;
            read.open(arguments);
            if (read.is_open())
            {
                stringstream fileInStream;
                while (read.peek() != EOF)
                {
                    fileInStream << (char)read.get();
                }
                string file = fileInStream.str();
                ofstream write;
                write.open(datablocks, std::fstream::app);
                int i = 0;
                int head = this->F.finding_Free();
                int current = head;
                while (i < file.size())
                {
                    F.setting_Value(current);	//busy bit is set
                    for (int j = 0; j<blockSize && i<file.size(); j++)
                        write << file[i++];
                    if (i < file.size())
                    {
                        F.setting_Next(current, F.finding_Free());
                        current = F.getting_Next(current);
                    }
                    else
                    {
                        F.setting_Next(current, -1);
                    }
                }
                (*pwd)[arguments] = head;
                this->paths.saving_State();
            }
            else
                cout << "fatal error: file not found\n";
        }
        else
            cout << "duplicate files are not allowed\n";
    }
    //for changing directories
    void cd(string arguments)
    {
        json* pwd = (paths.cwd->getNext());
        if (arguments.compare("..") == 0)
        {
            if (!track.empty())
            {
                paths.cwd = track.top();
                track.pop();
            }

        }
        else if (pwd->find(arguments) != pwd->end())
        {
            if ((*pwd)[arguments].is_number())
            {
                cout << "File can not be mounted\n";
            }
			else
            {
                track.push(paths.cwd);
                char* buffer = new char[arguments.size() + 1];
                arguments.copy(buffer, arguments.size() + 1);
                buffer[arguments.size()] = '\0';
                paths.cwd = new Rows<char *, json*>(buffer, &(*pwd)[arguments]);
            }

        }
        else
            cout << "Directory not found\n";
    }
   //removing the empty directories
    void rmdir(string arg)
    {
        json* pwd = (paths.cwd->getNext());
        if (pwd->find(arg) != pwd->end())
        {
            if ((*pwd)[arg].is_number())
            {
                removing_File((*pwd)[arg]);
            }
			else
            {
                removing_Dir((*pwd)[arg]);
            }
            (*pwd).erase(arg);
            this->paths.saving_State();

        }
        else
            cout << "Directory or file not found\n";
    }
    //removing the file
    void removing_File(int block)
    {
        int current = block;
        F.resetting_Value(current);
        while (F.getting_Next(current) != -1)
        {
            int next = F.getting_Next(current);
            F.reseting_Next(current);
            current = next;
            F.resetting_Value(current);
        }
    }
    //removing the directory
    void removing_Dir(json& root)
    {
        for (auto obj = root.begin(); obj != root.end(); obj++)
        {
            if (!obj.value().is_number()) {
                if (obj.value() != nullptr)
                {
                    removing_Dir(root[obj.key()]);
                }
            }
            else
                removing_File(obj.value());
        }
    }
   //for the parsing
    static void parser(File_System& f)
    {
        cout << f.paths.cwd->getValue() << "$ ";
        char buff[100];
        cin.getline(buff, 100);
        std::istringstream ss(buff);
        std::string token;
        string command;
        string arguments;
        string extra;
        std::getline(ss, command, ' ');
        std::getline(ss, arguments, ' ');
        std::getline(ss, extra, ' ');

        if (!extra.empty())
            cout << "too many arguments\n";
        else if (arguments.empty() && (command.compare("ls") != 0) && (command.compare("quit") != 0))
            cout << "too few arguments\n";
        else if (command.compare("rmdir") == 0)
        {
            f.rmdir(arguments);
        }

        else if (command.compare("ls") == 0)
        {
            f.ls();
        }

        else if (command.compare("mkdir") == 0)
        {
            f.mkdir(arguments);
        }

        else if (command.compare("cd") == 0)
        {
            f.cd(arguments);
        }

        else if (command.compare("import") == 0)
        {
            f.fetch(arguments);
        }
        else if (command.compare("quit") == 0)
        {
            return;
        }
        else
            cout << "Command not found\n";
        parser(f);
    }

    static void main(File_System& f)
    {
        parser(f);
    }

	~File_System()
{
    cout << "The Destructor has been called" << endl;
}
};

class Filing
{
    public:
    json root2;
    string File;

    Filing()
    {
        ofstream write;
        write.open(directory);
        write << root.dump(4);
        write.close();
    }

~Filing
    {
        cout<<"Destructor has been called"<<endl;
    }

};

int main(int argc,char * argv[])
        {

        File_System mySystem;
        File_System::main(mySystem);
        return 0;

        }