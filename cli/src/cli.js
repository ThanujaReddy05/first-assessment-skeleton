import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server
let session =  " "
let sessionContents
let clientPort
let clientHost

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username> [host] [port]')
  .delimiter(cli.chalk['green']('connected>'))
  .init(function (args, callback) {
    username = args.username
    clientHost = args.host 
    clientPort = args.port
    
    //connect to server with localhost and 8080 if it is not provided with connect command
    server = connect({ host: (clientHost === undefined)? 'localhost' : clientHost, 
                       port:(clientPort === undefined)? 8080 : clientPort }, () => {

      server.write(new Message({ username, command:'connect' }).toJSON() + '\n')
      callback()
    })


    server.on('data', (buffer) => {

      //Set color to each command
      if(Message.fromJSON(buffer).command === 'connect') 
        this.log(cli.chalk['blue'](Message.fromJSON(buffer).toString()))

      if(Message.fromJSON(buffer).command === 'echo') 
        this.log(cli.chalk['yellow'](Message.fromJSON(buffer).toString()))

      if(Message.fromJSON(buffer).command === 'broadcast') 
        this.log(cli.chalk['green'](Message.fromJSON(buffer).toString()))

      if(Message.fromJSON(buffer).command.includes('@') )
        this.log(cli.chalk['cyan'](Message.fromJSON(buffer).toString()))

      if(Message.fromJSON(buffer).command === 'users' ) 
        this.log(cli.chalk['magenta'](Message.fromJSON(buffer).toString()))

      if(Message.fromJSON(buffer).command === 'disconnect') 
        this.log(cli.chalk['gray'](Message.fromJSON(buffer).toString()))
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })


  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /\S+/g) //Regular expression to read @ symbol from input
    const contents = rest.join(' ')
    sessionContents = command + ' ' + contents

    if (command.includes('@') ) {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        //set same command untill different command is entered
        session = command
        this.delimiter(cli.chalk['green']('<Whisper>'))

    } else if (command === 'echo') {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        //set same command untill different command is entered
        session = command
        this.delimiter(cli.chalk['green']('<echo>'))
          
    }  
      else if (command === 'broadcast') {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        //set same command untill different command is entered
        session = command
        this.delimiter(cli.chalk['green']('<Broadcast>'))
    } 

      else if  (command === 'disconnect') {
        server.end(new Message({ username, command }).toJSON() + '\n')
        this.delimiter(cli.chalk['green']('<Disconnected>'))
    } 

      else if (command === 'users') {
        server.write(new Message({ username, command, contents }).toJSON() + '\n')
        this.delimiter(cli.chalk['green']('<Users>'))
    } 
      
      else if (session === 'echo' || session === 'broadcast' ||  session.includes('@'))  {
        server.write(new Message({ username: username, command: session, contents: sessionContents }).toJSON() + '\n')
    } 
    
      else {
        this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })



